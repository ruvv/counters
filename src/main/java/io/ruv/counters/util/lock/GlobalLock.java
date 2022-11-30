package io.ruv.counters.util.lock;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Simple class executing actions inside {@link ReentrantReadWriteLock}
 */
public final class GlobalLock {

    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();

    /**
     * Executes action inside write lock
     *
     * @param action action to execute
     * @param <T>    action result type
     * @return action result
     */
    public <T> T writing(Supplier<T> action) {

        globalLock.writeLock().lock();
        try {

            return action.get();
        } finally {

            globalLock.writeLock().unlock();
        }
    }

    /**
     * Executes action inside read lock
     *
     * @param action action to execute
     * @param <T>    action result type
     * @return action result
     */
    public <T> T reading(Supplier<T> action) {

        globalLock.readLock().lock();
        try {

            return action.get();
        } finally {

            globalLock.readLock().unlock();
        }
    }
}
