package io.ruv.counters.util.striped;

import com.google.common.util.concurrent.Striped;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ruv.counters.util.lock.GlobalLock;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

/**
 * Wraps {@link Striped} to add resize functionality
 * Background thread analyzes current load factor and grows/shrinks delegate {@link Striped}
 * Uses provided {@link GlobalLock} to protect resizing
 */
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class ResizingStriped {

    private static final int MIN_SIZE = 128;
    private static final int DELAY_SECONDS = 60;

    private final GlobalLock globalLock;
    private final Supplier<Integer> loadEstimator;

    private Striped<ReadWriteLock> delegate = Striped.lazyWeakReadWriteLock(MIN_SIZE);

    public ResizingStriped(GlobalLock globalLock, Supplier<Integer> loadEstimator) {

        this.globalLock = globalLock;
        this.loadEstimator = loadEstimator;

        val threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("ResizingStripedDaemon-")
                .build();

        @SuppressWarnings("resource") // only runs daemon thread
        val executor = Executors.newSingleThreadScheduledExecutor(threadFactory);

        executor.scheduleWithFixedDelay(this::resizeAsNeeded, DELAY_SECONDS, DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @NonNull
    public ReadWriteLock get(@NonNull Object o) {

        return delegate.get(o);
    }

    private void resizeAsNeeded() {

        val stripedSize = delegate.size();
        val mapSize = loadEstimator.get();
        if (mapSize == 0) {
            return;
        }

        val loadFactor = (double) stripedSize / mapSize;

        if (loadFactor > .75) {

            globalLock.writing(() -> this.delegate = Striped.lazyWeakReadWriteLock(stripedSize * 2));
        } else if (loadFactor < .25) {

            if (stripedSize > MIN_SIZE) {

                globalLock.writing(() -> this.delegate = Striped.lazyWeakReadWriteLock(stripedSize / 2));
            }
        }
    }
}
