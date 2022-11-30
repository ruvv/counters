package io.ruv.counters.repo.striped;

import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.MapBasedCountersRepositoryTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

public class StripedLongCountersRepositoryTest implements MapBasedCountersRepositoryTest<Long> {

    private final StripedLongCountersRepository repository = new StripedLongCountersRepository();

    @Override
    public CountersRepository repository() {

        return repository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Long> internalStorage() {

        return (Map<String, Long>) ReflectionTestUtils.getField(repository, "storage");
    }

    @Override
    public long valueExtractor(Long container) {

        return container;
    }

    @Override
    public Long oneValue() {

        return 0L;
    }

    @Override
    public Long anotherValue() {

        return 10L;
    }

    @Override
    public Long overflowingValue() {

        return Long.MAX_VALUE;
    }
}
