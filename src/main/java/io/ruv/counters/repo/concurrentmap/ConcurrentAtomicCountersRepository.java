package io.ruv.counters.repo.concurrentmap;

import io.ruv.counters.repo.Counter;
import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.SimpleCounter;
import io.ruv.counters.util.lock.GlobalLock;
import lombok.val;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Implementation of {@link CountersRepository} backed by {@link ConcurrentHashMap} with {@link AtomicLong} values
 * {@link GlobalLock} is used to block modifications while creating a copy in {@linkplain #findAll()}
 */
public class ConcurrentAtomicCountersRepository implements CountersRepository {

    private final Map<String, AtomicLong> storage = new ConcurrentHashMap<>();
    private final GlobalLock global = new GlobalLock();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Optional<Counter> findByName(@NonNull String name) {

        val container = storage.get(name);

        if (container == null) {

            return Optional.empty();
        } else {

            return Optional.of(new SimpleCounter(name, container.get()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Stream<Counter> findAll() {

        val copy = global.writing(() -> new HashMap<>(storage));

        return copy.entrySet().stream().map(this::entryToCounter);
    }

    private Counter entryToCounter(Map.Entry<String, AtomicLong> entry) {

        return new SimpleCounter(entry.getKey(), entry.getValue().get());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> create(@NonNull String name, long value) {

        return global.reading(() -> {

            val container = new AtomicLong(value);

            if (storage.putIfAbsent(name, container) == null) {

                return Optional.of(new SimpleCounter(name, value));
            } else {

                return Optional.empty();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> incrementByName(@NonNull String name) {

        return global.reading(() -> {

            final SimpleCounter[] counter = new SimpleCounter[1];

            val compResult = storage.compute(name, (key, container) -> {

                if (container == null) {

                    return null;
                } else {

                    val currentValue = container.get();
                    if (currentValue == Long.MAX_VALUE) {

                        throw new ArithmeticException("long overflow");
                    } else {

                        // container is mutable - save value as side effect of atomic operation
                        counter[0] = new SimpleCounter(name, container.incrementAndGet());
                        return container;
                    }
                }
            });

            if (compResult == null) {

                return Optional.empty();
            } else {

                return Optional.of(counter[0]);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> deleteByName(@NonNull String name) {

        return global.reading(() -> {

            val container = storage.remove(name);

            if (container == null) {

                return Optional.empty();
            } else {

                return Optional.of(new SimpleCounter(name, container.get()));
            }
        });
    }
}
