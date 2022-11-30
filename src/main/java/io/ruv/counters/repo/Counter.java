package io.ruv.counters.repo;

import org.springframework.lang.NonNull;

/**
 * Counter abstraction allowing for different {@link CountersRepository} implementations
 * Contains optional locking release handle for pseudo-atomic access support
 */
public interface Counter {

    @NonNull
    String getName();

    @NonNull
    long getValue();

    /**
     * Release locking mechanism (if any) over this counter
     */
    default void release() {
        // nothing
    }
}
