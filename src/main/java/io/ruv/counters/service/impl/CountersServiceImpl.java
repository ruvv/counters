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
    public static final int MAX_NAME_LENGTH = 1024; //todo externalize

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto create(@NonNull CounterDto counter) throws DuplicateNameException, IllegalNameException {

        creationPreconditions(counter);

        val result = repository.create(counter.getName(), counter.getValue());
        return result.map(this::toDto)
                .orElseThrow(() -> DuplicateNameException.of(counter.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto incrementByName(@NonNull String name) throws NotFoundException, OverflowException {

        try {

            val result = repository.incrementByName(name);

            return result.map(this::toDto).orElseThrow(() -> NotFoundException.of(name));
        } catch (ArithmeticException e) {

            throw OverflowException.of(name, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto getByName(@NonNull String name) throws NotFoundException {

        val result = repository.findByName(name);

        return result.map(this::toDto).orElseThrow(() -> NotFoundException.of(name));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CounterDto deleteByName(@NonNull String name) throws NotFoundException {

        val result = repository.deleteByName(name);

        return result.map(this::toDto).orElseThrow(() -> NotFoundException.of(name));
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

            throw IllegalNameException.nullName();
        }

        if (dto.getName().isEmpty()) {

            throw IllegalNameException.emptyName();
        }

        if (dto.getName().length() > MAX_NAME_LENGTH) {


            throw IllegalNameException.tooLongName(dto.getName(), MAX_NAME_LENGTH);
        }
    }

    private CounterDto toDto(Counter counter) {

        val dto = new CounterDto();
        dto.setName(counter.getName());
        dto.setValue(counter.getValue());

        return dto;
    }
}
