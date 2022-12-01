package io.ruv.counters.service.impl;

import io.ruv.counters.repo.Counter;
import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.service.*;
import io.ruv.counters.web.dto.CounterDto;
import io.ruv.counters.web.dto.CounterNamesDto;
import io.ruv.counters.web.dto.CounterSumDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.NonNull;

import java.math.BigInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CountersServiceImpl implements CountersService {

    private final CountersRepository repository;

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto create(@NonNull CounterDto counter) throws DuplicateNameException, IllegalNameException {

        creationPreconditions(counter);

        val result = repository.create(counter.getName(), counter.getValue());
        return result.map(this::toDto)
                .orElseThrow(() -> new DuplicateNameException(
                        String.format("Counter with name '%s' already exists.", counter.getName())));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto incrementByName(@NonNull String name) throws NotFoundException, OverflowException {

        try {

            val result = repository.incrementByName(name);

            return result.map(this::toDto).orElseThrow(() -> notFound(name));
        } catch (ArithmeticException e) {

            throw new OverflowException(String.format("Counter '%s' can not be incremented without overflowing.", name), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto getByName(@NonNull String name) throws NotFoundException {

        val result = repository.findByName(name);

        return result.map(this::toDto).orElseThrow(() -> notFound(name));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto deleteByName(@NonNull String name) throws NotFoundException {

        val result = repository.deleteByName(name);

        return result.map(this::toDto).orElseThrow(() -> notFound(name));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterSumDto getCounterSum() {

        var sum = BigInteger.ZERO;

        return new CounterSumDto(repository.findAll()
                .map(Counter::getValue)
                .map(BigInteger::valueOf)
                .reduce(sum, BigInteger::add));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterNamesDto getCounterNames() {

        return new CounterNamesDto(repository.findAll()
                .map(Counter::getName)
                .collect(Collectors.toList()));
    }

    private void creationPreconditions(CounterDto dto) {

        if (dto.getName() == null) {

            throw new IllegalNameException("Counter name was not specified.");
        }

        if (dto.getName().isEmpty()) {

            throw new IllegalNameException(String.format("Illegal counter name '%s'.", dto.getName()));
        }
    }

    private CounterDto toDto(Counter counter) {

        val dto = new CounterDto();
        dto.setName(counter.getName());
        dto.setValue(counter.getValue());

        return dto;
    }

    private NotFoundException notFound(String name) {

        return new NotFoundException(String.format("Counter with name '%s' does not exist.", name));
    }
}
