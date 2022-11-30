package io.ruv.counters.repo.striped;

import com.google.common.util.concurrent.Striped;
import io.ruv.counters.repo.Counter;
import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.SimpleCounter;
import io.ruv.counters.util.lock.GlobalLock;
import io.ruv.counters.util.striped.ResizingStriped;
import lombok.val;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of {@link CountersRepository} backed by {@link HashMap} with long[] values
 * using {@link Striped} as a local locking mechanism
 * {@link GlobalLock} is used to block modifications while creating a copy in {@linkplain #findAll()}
 */
@SuppressWarnings("UnstableApiUsage")
public class StripedLongArrayCountersRepository implements CountersRepository {


    private final GlobalLock global = new GlobalLock();
    private final Map<String, long[]> storage = new HashMap<>();
    private final ResizingStriped locks = new ResizingStriped(global, storage::size);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Optional<Counter> findByName(@NonNull String name) {

        val lock = locks.get(name).readLock();
        lock.lock();
        try {

            val container = storage.get(name);

            if (container == null) {

                return Optional.empty();
            } else {

                val value = container[0];

                return Optional.of(new SimpleCounter(name, value));
            }
        } finally {

            lock.unlock();
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

    private Counter entryToCounter(Map.Entry<String, long[]> entry) {

        return new SimpleCounter(entry.getKey(), entry.getValue()[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Optional<Counter> create(@NonNull String name, @NonNull long value) {

        return global.reading(() -> {

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.get(name);

                if (container == null) {

                    storage.put(name, new long[]{value});
                    return Optional.of(new SimpleCounter(name, value));
                } else {

                    return Optional.empty();
                }
            } finally {

                lock.unlock();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Optional<Counter> increment(@NonNull String name) {

        return global.reading(() -> {

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.get(name);

                if (container == null) {

                    return Optional.empty();
                } else {

                    container[0] = Math.incrementExact(container[0]);
                    return Optional.of(new SimpleCounter(name, container[0]));
                }
            } finally {

                lock.unlock();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Optional<Counter> delete(@NonNull String name) {

        return global.reading(() -> {

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.remove(name);

                if (container == null) {

                    return Optional.empty();
                } else {

                    return Optional.of(new SimpleCounter(name, container[0]));
                }
            } finally {

                lock.unlock();
            }
        });
    }
}
