package io.ruv.counters.service;

import io.ruv.counters.web.dto.CounterDto;
import io.ruv.counters.web.dto.CounterNamesDto;
import io.ruv.counters.web.dto.CounterSumDto;
import org.springframework.lang.NonNull;

/**
 * Interface detailing counter operation contracts
 */
public interface CountersService {

    /**
     * Creates new counter with name and initial value specified in parameter
     *
     * @param counter name and initial value container
     * @return created counter
     * @throws DuplicateNameException when counter with specified name already exists
     * @throws IllegalNameException   when specified name is null or empty
     */
    @NonNull
    CounterDto create(@NonNull CounterDto counter) throws DuplicateNameException, IllegalNameException;

    /**
     * Increments value for counter with specified name
     *
     * @param name target counter name
     * @return updated counter
     * @throws NotFoundException when counter with specified name does not exist
     * @throws OverflowException when incrementing specified counter will result in long value overflow
     */
    @NonNull
    CounterDto incrementByName(@NonNull String name) throws NotFoundException, OverflowException;

    /**
     * Retrieves counter with specified name
     *
     * @param name target counter name
     * @return counter with specified name
     * @throws NotFoundException when counter with specified name does not exist
     */
    @NonNull
    CounterDto getByName(@NonNull String name) throws NotFoundException;

    /**
     * Deletes counter with specified name
     *
     * @param name target counter name
     * @return deleted counter
     * @throws NotFoundException when counter with specified name does not exist
     */
    CounterDto deleteByName(@NonNull String name) throws NotFoundException;

    /**
     * Retrieves sum of every counter present at the start of operation
     * When no counters exist at the start of operation - returns 0 (product of empty counter list)
     *
     * @return container with sum of counter values
     */
    @NonNull
    CounterSumDto getCounterSum();

    /**
     * Returns names of every counter present at the start of operation
     * When no counters exist at the start of operation - returns empty list
     *
     * @return container with counter names
     */
    @NonNull
    CounterNamesDto getCounterNames();
}
