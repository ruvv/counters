package io.ruv.counters.repo.concurrentmap;

import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.MapBasedCountersRepositoryTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentAtomicCountersRepositoryTest implements MapBasedCountersRepositoryTest<AtomicLong> {

    private final ConcurrentAtomicCountersRepository repository = new ConcurrentAtomicCountersRepository();


    @Override
    public CountersRepository repository() {

        return repository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, AtomicLong> internalStorage() {

        return (Map<String, AtomicLong>) ReflectionTestUtils.getField(repository, "storage");
    }

    @Override
    public long valueExtractor(AtomicLong container) {

        return container.get();
    }

    @Override
    public AtomicLong oneValue() {

        return new AtomicLong(0);
    }

    @Override
    public AtomicLong anotherValue() {

        return new AtomicLong(10);
    }

    @Override
    public AtomicLong overflowingValue() {

        return new AtomicLong(Long.MAX_VALUE);
    }
}
