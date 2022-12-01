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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Implementation of {@link CountersRepository} backed by {@link HashMap} with {@link AtomicLong} values
 * using {@link Striped} as a local locking mechanism
 * {@link GlobalLock} is used to block modifications while creating a copy in {@linkplain #findAll()}
 */
@SuppressWarnings("UnstableApiUsage")
public class StripedAtomicCountersRepository implements CountersRepository {


    private final GlobalLock global = new GlobalLock();
    private final HashMap<String, AtomicLong> storage = new HashMap<>();
    private final ResizingStriped locks = new ResizingStriped(global, storage::size);

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> findByName(@NonNull String name) {

        val lock = locks.get(name).readLock();
        lock.lock();
        try {

            val container = storage.get(name);
            if (container == null) {

                return Optional.empty();
            } else {

                return Optional.of(new SimpleCounter(name, container.get()));
            }
        } finally {

            lock.unlock();
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

    private Counter entryToCounter(Map.Entry<String, AtomicLong> entry) {

        return new SimpleCounter(entry.getKey(), entry.getValue().get());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Optional<Counter> create(@NonNull String name, @NonNull long value) {

        return global.reading(() -> {

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.get(name);
                if (container == null) {

                    storage.put(name, new AtomicLong(value));
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
    @NonNull
    @Override
    public Optional<Counter> incrementByName(@NonNull String name) {

        return global.reading(() -> {

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.get(name);
                if (container == null) {

                    return Optional.empty();
                } else {

                    val currentValue = container.get();
                    if (currentValue == Long.MAX_VALUE) {

                        throw new ArithmeticException("long overflow");
                    } else {

                        return Optional.of(new SimpleCounter(name, container.incrementAndGet()));
                    }
                }
            } finally {

                lock.unlock();
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

            val lock = locks.get(name).writeLock();
            lock.lock();
            try {

                val container = storage.get(name);
                if (container == null) {

                    return Optional.empty();
                } else {

                    storage.remove(name);
                    return Optional.of(new SimpleCounter(name, container.get()));
                }
            } finally {

                lock.unlock();
            }
        });
    }
}
