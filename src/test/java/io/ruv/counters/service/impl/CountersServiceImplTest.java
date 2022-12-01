package io.ruv.counters.service.impl;

import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.SimpleCounter;
import io.ruv.counters.service.DuplicateNameException;
import io.ruv.counters.service.IllegalNameException;
import io.ruv.counters.service.NotFoundException;
import io.ruv.counters.service.OverflowException;
import io.ruv.counters.web.dto.CounterDto;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CountersServiceImplTest {

    private final CountersRepository repo = Mockito.mock(CountersRepository.class);

    private final CountersServiceImpl service = new CountersServiceImpl(repo);

    private final String oneName = "one-name";
    private final long oneValue = 0;
    private final String anotherName = "another-name";
    private final long anotherValue = 10;

    private final CounterDto oneDto = new CounterDto() {
        {
            setName(oneName);
            setValue(oneValue);
        }
    };

    @AfterEach
    public void reset() {

        Mockito.reset(repo);
    }

    @Test
    public void createNonExistingReturnsCounter() {

        Mockito.when(repo.create(Mockito.any(), Mockito.anyLong()))
                .thenReturn(Optional.of(new SimpleCounter(oneName, oneValue)));

        //act
        val result = service.create(oneDto);

        Assertions.assertThat(result).isEqualTo(oneDto);
    }

    @Test
    public void createExistingThrowsException() {

        Mockito.when(repo.create(Mockito.any(), Mockito.anyLong()))
                .thenReturn(Optional.empty());

        //act
        Assertions.assertThatThrownBy(() -> service.create(oneDto))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessageContaining(oneName);
    }

    @Test
    public void createNullNameThrowsException() {

        val dto = new CounterDto();

        //act
        Assertions.assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalNameException.class);
    }

    @Test
    public void createEmptyNameThrowsException() {

        val dto = new CounterDto();
        dto.setName("");

        //act
        Assertions.assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalNameException.class);
    }

    @Test
    public void incrementExistingReturnsModifiedRecord() {

        val expected = new CounterDto();
        expected.setName(oneName);
        expected.setValue(oneValue + 1);

        Mockito.when(repo.incrementByName(Mockito.any()))
                .thenAnswer(invocationOnMock -> Optional.of(
                        new SimpleCounter(invocationOnMock.getArgument(0), oneValue + 1)));

        //act
        val result = service.incrementByName(oneName);

        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    public void incrementNonExistingThrowsException() {

        Mockito.when(repo.incrementByName(Mockito.any()))
                .thenReturn(Optional.empty());

        //act
        Assertions.assertThatThrownBy(() -> service.incrementByName(oneName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(oneName);
    }

    @Test
    public void incrementOverflowingThrowsException() {

        Mockito.when(repo.incrementByName(Mockito.any()))
                .thenThrow(new ArithmeticException());

        //act
        Assertions.assertThatThrownBy(() -> service.incrementByName(oneName))
                .isInstanceOf(OverflowException.class)
                .hasMessageContaining(oneName);
    }

    @Test
    public void getExistingReturnsRecord() {

        Mockito.when(repo.findByName(Mockito.any()))
                .thenReturn(Optional.of(new SimpleCounter(oneName, oneValue)));

        //act
        val result = service.getByName(oneName);

        Assertions.assertThat(result).isEqualTo(oneDto);
    }

    @Test
    public void getNonExistingThrowsException() {

        Mockito.when(repo.findByName(Mockito.any()))
                .thenReturn(Optional.empty());

        //act
        Assertions.assertThatThrownBy(() -> service.getByName(oneName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(oneName);
    }

    @Test
    public void deleteExistingReturnsRecord() {

        Mockito.when(repo.deleteByName(Mockito.any()))
                .thenReturn(Optional.of(new SimpleCounter(oneName, oneValue)));

        //act
        val result = service.deleteByName(oneName);

        Assertions.assertThat(result).isEqualTo(oneDto);
    }

    @Test
    public void deleteNonExistingThrowsException() {

        Mockito.when(repo.deleteByName(Mockito.any()))
                .thenReturn(Optional.empty());

        //act
        Assertions.assertThatThrownBy(() -> service.deleteByName(oneName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(oneName);
    }

    @Test
    public void getCountersSumEmptyReturnsZero() {

        Mockito.when(repo.findAll())
                .thenReturn(Stream.empty());

        //act
        val result = service.getCounterSum();

        Assertions.assertThat(result.getSum()).isEqualTo(BigInteger.ZERO);
    }

    @Test
    public void getCountersSumNonEmptyReturnsSum() {

        Mockito.when(repo.findAll())
                .thenReturn(Stream.of(new SimpleCounter(oneName, oneValue),
                        new SimpleCounter(anotherName, anotherValue)));

        //act
        val result = service.getCounterSum();

        Assertions.assertThat(result.getSum()).isEqualTo(BigInteger.valueOf(oneValue + anotherValue));
    }

    @Test
    public void getCounterNamesEmptyReturnsEmptyList() {

        Mockito.when(repo.findAll())
                .thenReturn(Stream.empty());

        //act
        val result = service.getCounterNames();

        Assertions.assertThat(result.getNames()).isEmpty();
    }

    @Test
    public void getCounterNamesNonEmptyReturnsNamesList() {

        Mockito.when(repo.findAll())
                .thenReturn(Stream.of(new SimpleCounter(oneName, oneValue),
                        new SimpleCounter(anotherName, anotherValue)));

        //act
        val result = service.getCounterNames();

        Assertions.assertThat(result.getNames()).isEqualTo(List.of(oneName, anotherName));
    }
}
