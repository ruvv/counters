package io.ruv.counters.repo;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


public interface MapBasedCountersRepositoryTest<T> {

    CountersRepository repository();

    Map<String, T> internalStorage();

    @AfterEach
    default void clearStorage() {

        internalStorage().clear();
    }

    long valueExtractor(T container);

    default String oneName() {

        return "one-name";
    }

    T oneValue();

    T anotherValue();

    T overflowingValue();

    @Test
    default void findByNameExistingReturnsCounter() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();

        storage.put(name, value);

        // act
        val maybeCounter = repo.findByName(name);


        Assertions.assertThat(maybeCounter).isNotEmpty();
        val counter = maybeCounter.get();

        Assertions.assertThat(counter.getName()).isEqualTo(name);
        Assertions.assertThat(counter.getValue()).isEqualTo(valueExtractor(value));
    }

    @Test
    default void findByNameNonExistingReturnsEmpty() {

        val repo = repository();

        // act
        val maybeCounter = repo.findByName(oneName());


        Assertions.assertThat(maybeCounter).isEmpty();
    }

    @Test
    default void findAllNonEmptyReturnsCountersStream() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();

        storage.put(name, value);

        //act
        val stream = repo.findAll();


        Assertions.assertThat(stream).singleElement()
                .extracting(Counter::getName, Counter::getValue)
                .isEqualTo(List.of(name, valueExtractor(value)));
    }

    @Test
    default void findAllEmptyReturnsEmptyStream() {

        val repo = repository();

        //act
        val stream = repo.findAll();


        Assertions.assertThat(stream).isEmpty();
    }

    @Test
    default void createNonExistingStoresRecordAndReturnsCounter() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();

        //act
        val maybeCounter = repo.create(name, valueExtractor(value));

        Assertions.assertThat(storage).hasSize(1);
        Assertions.assertThat(valueExtractor(storage.get(name))).isEqualTo(valueExtractor(value));

        Assertions.assertThat(maybeCounter).isNotEmpty();
        val counter = maybeCounter.get();

        Assertions.assertThat(counter.getName()).isEqualTo(name);
        Assertions.assertThat(counter.getValue()).isEqualTo(valueExtractor(value));
    }

    @Test
    default void createExistingDoesNotOverwriteAndReturnsEmpty() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();
        val anotherValue = anotherValue();

        storage.put(name, value);

        //act
        val maybeCounter = repo.create(name, valueExtractor(anotherValue));

        Assertions.assertThat(storage).hasSize(1);
        Assertions.assertThat(storage.get(name)).isEqualTo(value);

        Assertions.assertThat(maybeCounter).isEmpty();
    }

    @Test
    default void incrementExistingModifiesRecordAndReturnsCounter() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();
        val expectedValue = valueExtractor(value) + 1;

        storage.put(name, value);

        //act
        val maybeCounter = repo.incrementByName(name);

        Assertions.assertThat(storage).hasSize(1);
        Assertions.assertThat(storage.get(name)).extracting(this::valueExtractor).isEqualTo(expectedValue);

        Assertions.assertThat(maybeCounter).isNotEmpty();
        val counter = maybeCounter.get();

        Assertions.assertThat(counter.getName()).isEqualTo(name);
        Assertions.assertThat(counter.getValue()).isEqualTo(expectedValue);
    }

    @Test
    default void incrementNonExistingReturnsEmpty() {

        val repo = repository();

        val name = oneName();

        //act
        val maybeCounter = repo.incrementByName(name);

        Assertions.assertThat(maybeCounter).isEmpty();
    }

    @Test
    default void incrementOverflowingThrowsException() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = overflowingValue();

        storage.put(name, value);

        //act
        Assertions.assertThatThrownBy(() -> repo.incrementByName(name))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    default void deleteExistingRemovesRecordReturnsCounter() {

        val repo = repository();
        val storage = internalStorage();

        val name = oneName();
        val value = oneValue();

        storage.put(name, value);

        //act
        val maybeCounter = repo.deleteByName(name);

        Assertions.assertThat(storage).isEmpty();

        Assertions.assertThat(maybeCounter).isNotEmpty();
        val counter = maybeCounter.get();

        Assertions.assertThat(counter.getName()).isEqualTo(name);
        Assertions.assertThat(counter.getValue()).isEqualTo(valueExtractor(value));
    }

    @Test
    default void deleteNonExistingReturnsEmpty() {

        val repo = repository();

        val name = oneName();

        //act
        val maybeCounter = repo.deleteByName(name);

        Assertions.assertThat(maybeCounter).isEmpty();
    }
}
