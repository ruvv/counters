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
import java.util.stream.Stream;

/**
 * Implementation of {@link CountersRepository} backed by {@link ConcurrentHashMap} with long[] values
 * {@link GlobalLock} is used to block modifications while creating a copy in {@linkplain #findAll()}
 */
public class ConcurrentLongArrayCountersRepository implements CountersRepository {

    private final ConcurrentHashMap<String, long[]> storage = new ConcurrentHashMap<>();
    private final GlobalLock global = new GlobalLock();

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> findByName(@NonNull String name) {

        val container = storage.get(name);

        if (container == null) {

            return Optional.empty();
        } else {

            return Optional.of(new SimpleCounter(name, container[0]));
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Stream<Counter> findAll() {

        val copy = global.writing(() -> new HashMap<>(storage));

        return copy.entrySet().stream().map(this::entryToCounter);
    }

    private Counter entryToCounter(Map.Entry<String, long[]> entry) {

        return new SimpleCounter(entry.getKey(), entry.getValue()[0]);
    }


    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> create(@NonNull String name, @NonNull long value) {

        return global.reading(() -> {

            val container = new long[]{value};

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
    public Optional<Counter> increment(@NonNull String name) {

        return global.reading(() -> {

            final SimpleCounter[] counter = new SimpleCounter[1];

            val compResult = storage.compute(name, (key, container) -> {

                if (container == null) {

                    return null;
                } else {

                    container[0] = Math.incrementExact(container[0]);

                    // container is mutable - save value as side effect of atomic operation
                    counter[0] = new SimpleCounter(name, container[0]);
                    return container;
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
    public Optional<Counter> delete(@NonNull String name) {

        return global.reading(() -> {

            val container = storage.remove(name);

            if (container == null) {

                return Optional.empty();
            } else {

                return Optional.of(new SimpleCounter(name, container[0]));
            }
        });
    }
}
