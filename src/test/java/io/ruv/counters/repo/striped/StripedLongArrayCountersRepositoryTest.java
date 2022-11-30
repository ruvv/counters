package io.ruv.counters.repo.striped;

import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.MapBasedCountersRepositoryTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

public class StripedLongArrayCountersRepositoryTest implements MapBasedCountersRepositoryTest<long[]> {

    private final StripedLongArrayCountersRepository repository = new StripedLongArrayCountersRepository();

    @Override
    public CountersRepository repository() {

        return repository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, long[]> internalStorage() {

        return (Map<String, long[]>) ReflectionTestUtils.getField(repository, "storage");
    }

    @Override
    public long valueExtractor(long[] container) {

        return container[0];
    }

    @Override
    public long[] oneValue() {

        return new long[]{0};
    }

    @Override
    public long[] anotherValue() {
        return new long[]{10};
    }

    @Override
    public long[] overflowingValue() {
        return new long[]{Long.MAX_VALUE};
    }
}
