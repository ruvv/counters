package io.ruv.counters.repo;

import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface detailing counter repository contracts
 * All methods return {@link Counter} abstraction, possibly locked {@linkplain Counter#release()}
 */
public interface CountersRepository {

    /**
     * Retrieves counter specified by name
     *
     * @param name target counter name
     * @return {@link Optional} containing target counter
     * or empty {@link Optional} if counter with specified name does not exist
     */
    @NonNull
    Optional<Counter> findByName(@NonNull String name);

    /**
     * Retrieves stream containing all counters present at the start of operation
     * If there are no counters present - returns empty stream
     * Depending on implementation counter values may be modified between start of this operation and moment
     * when resulting stream is consumed
     *
     * @return {@link Stream} containing counters present at the start of operation
     */
    @NonNull
    Stream<Counter> findAll();

    /**
     * Creates new counter with specified name and initial value if no counter with same name already exists
     *
     * @param name  new counter name
     * @param value new counter value
     * @return {@link Optional} containing created counter
     * or empty {@link Optional} if counter with specified name already exists
     */
    @NonNull
    Optional<Counter> create(@NonNull String name, @NonNull long value);

    /**
     * Increments value of counter with specified name if it exists
     *
     * @param name target counter name
     * @return {@link Optional} containing updated counter
     * or empty {@link Optional} if counter with specified name does not exist
     * @throws ArithmeticException when incrementing counter value will result in overflow
     */
    @NonNull
    Optional<Counter> incrementByName(@NonNull String name) throws ArithmeticException;

    /**
     * Removes and returns counter with specified name if it exists
     *
     * @param name target counter name
     * @return {@link Optional} containing removed counter
     * or empty {@link Optional} if counter with specified name does not exist
     */
    @NonNull
    Optional<Counter> deleteByName(@NonNull String name);

}
