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
 * Implementation of {@link CountersRepository} backed by {@link ConcurrentHashMap} with {@link Long} values
 * {@link GlobalLock} is used to block modifications while creating a copy in {@linkplain #findAll()}
 */
public class ConcurrentLongCountersRepository implements CountersRepository {

    private final ConcurrentHashMap<String, Long> storage = new ConcurrentHashMap<>();
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

            return Optional.of(new SimpleCounter(name, container));
        }
    }

    /**
     * {@inheritDoc}
     * Immutable value container guarantees that counter values will not change between start of this operation
     * and the moment when resulting stream is consumed
     */
    @NonNull
    @Override
    public Stream<Counter> findAll() {

        val copy = global.writing(() -> new HashMap<>(storage));

        return copy.entrySet().stream().map(this::entryToCounter);
    }

    private Counter entryToCounter(Map.Entry<String, Long> entry) {

        return new SimpleCounter(entry.getKey(), entry.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> create(@NonNull String name, long value) {

        return global.reading(() -> {

            if (storage.putIfAbsent(name, value) == null) {

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

            val container = storage.compute(name, (key, value) -> {

                if (value == null) {

                    return null;
                } else {

                    return Math.incrementExact(value);
                }
            });

            if (container == null) {

                return Optional.empty();
            } else {

                return Optional.of(new SimpleCounter(name, container));
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

                return Optional.of(new SimpleCounter(name, container));
            }
        });
    }
}
