package io.ruv.counters.evaluation;

import com.google.common.collect.Iterables;
import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.concurrentmap.ConcurrentAtomicCountersRepository;
import io.ruv.counters.repo.concurrentmap.ConcurrentLongArrayCountersRepository;
import io.ruv.counters.repo.concurrentmap.ConcurrentLongCountersRepository;
import io.ruv.counters.repo.striped.StripedAtomicCountersRepository;
import io.ruv.counters.repo.striped.StripedLongArrayCountersRepository;
import io.ruv.counters.repo.striped.StripedLongCountersRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
@Disabled
@Slf4j
public class RepoEvaluationTest {

    private static final int HEAT_UP_MINUTES = 1;
    private static final int ROLL_MINUTES = 1;

    private final List<CountersRepository> implementations = List.of(
            new ConcurrentLongCountersRepository(),
            new StripedLongCountersRepository(),
            new ConcurrentAtomicCountersRepository(),
            new StripedAtomicCountersRepository(),
            new ConcurrentLongArrayCountersRepository(),
            new StripedLongArrayCountersRepository());

    private final List<String> lowNames = makeSomeNames(64);

    private final List<String> mediumNames = makeSomeNames(512);

    private final List<String> highNames = makeSomeNames(2048);

    private List<String> makeSomeNames(int count) {

        val result = new ArrayList<String>(count);

        while (result.size() < count) {

            result.add(String.valueOf(ThreadLocalRandom.current().nextInt()));
        }

        return result;
    }

    private void findSomething(CountersRepository repo, List<String> names) {

        repo.findByName(names.get(ThreadLocalRandom.current().nextInt(names.size())));
    }

    private void createSomething(CountersRepository repo, List<String> names) {

        repo.create(names.get(ThreadLocalRandom.current().nextInt(names.size())), ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    }

    private void incrementSomething(CountersRepository repo, List<String> names) {

        try {

            repo.incrementByName(names.get(ThreadLocalRandom.current().nextInt(names.size())));
        } catch (ArithmeticException e) {
            //whatever
        }
    }

    private void deleteSomething(CountersRepository repo, List<String> names) {

        repo.deleteByName(names.get(ThreadLocalRandom.current().nextInt(names.size())));
    }

    private void findAll(CountersRepository repo, List<String> names) {

        repo.findAll();
    }


    private final List<BiConsumer<CountersRepository, List<String>>> tenReads = Collections.nCopies(10, this::findSomething);
    private final List<BiConsumer<CountersRepository, List<String>>> tenCreates = Collections.nCopies(10, this::createSomething);
    private final List<BiConsumer<CountersRepository, List<String>>> tenIncrements = Collections.nCopies(10, this::incrementSomething);
    private final List<BiConsumer<CountersRepository, List<String>>> tenDeletes = Collections.nCopies(10, this::deleteSomething);
    private final List<BiConsumer<CountersRepository, List<String>>> tenGlobalCalls = Collections.nCopies(10, this::findAll);

    private final List<BiConsumer<CountersRepository, List<String>>> mostlyReads = new ArrayList<>() {{

        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenCreates);
        addAll(tenIncrements);
        addAll(tenDeletes);
        addAll(tenGlobalCalls);
        Collections.shuffle(this);
    }};

    private final List<BiConsumer<CountersRepository, List<String>>> readsAndWrites = new ArrayList<>() {{

        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenCreates);
        addAll(tenCreates);
        addAll(tenIncrements);
        addAll(tenIncrements);
        addAll(tenDeletes);
        addAll(tenDeletes);
        addAll(tenGlobalCalls);
        Collections.shuffle(this);
    }};

    private final List<BiConsumer<CountersRepository, List<String>>> mostlyWrites = new ArrayList<>() {{

        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenCreates);
        addAll(tenCreates);
        addAll(tenCreates);
        addAll(tenIncrements);
        addAll(tenIncrements);
        addAll(tenIncrements);
        addAll(tenDeletes);
        addAll(tenDeletes);
        addAll(tenDeletes);
        addAll(tenGlobalCalls);
        Collections.shuffle(this);
    }};

    private final List<BiConsumer<CountersRepository, List<String>>> moreGlobalLocking = new ArrayList<>() {{

        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenReads);
        addAll(tenCreates);
        addAll(tenIncrements);
        addAll(tenDeletes);
        addAll(tenGlobalCalls);
        addAll(tenGlobalCalls);
        addAll(tenGlobalCalls);
        addAll(tenGlobalCalls);
        addAll(tenGlobalCalls);
        Collections.shuffle(this);
    }};

    private final Map<String, List<BiConsumer<CountersRepository, List<String>>>> mixtures = Map.of(

            "mostlyReads", mostlyReads,
            "readsAndWrites", readsAndWrites,
            "mostlyWrites", mostlyWrites,
            "moreGlobalLocking", moreGlobalLocking);


    private void heatUp(CountersRepository repo, List<String> names, List<BiConsumer<CountersRepository, List<String>>> mixture) {

        roll(repo, names, mixture, 8, HEAT_UP_MINUTES);
    }

    private long roll(CountersRepository repo,
                      List<String> names,
                      List<BiConsumer<CountersRepository, List<String>>> mixture,
                      int threads,
                      int minutes) {

        val collector = new AtomicLong(0);
        val enough = new AtomicBoolean(false);

        try (val executor = Executors.newFixedThreadPool(threads)) {

            for (int i = 0; i < threads; i++) {

                executor.submit(() -> {

                    val endless = Iterables.cycle(mixture).iterator();
                    while (!enough.get()) {

                        endless.next().accept(repo, names);
                        collector.incrementAndGet();
                    }
                });
            }

            Thread.currentThread().join(Duration.ofMinutes(minutes));
            enough.set(true);

            return collector.get();
        } catch (InterruptedException e) {

            throw new RuntimeException("Interrupted, exiting.", e);
        }
    }

    @Test
    public void lowNames() {

        log.info("Low names");

        for (var entry : mixtures.entrySet()) {

            val mixture = entry.getValue();

            log.info("Mixture {}:", entry.getKey());

            val result = new LinkedHashMap<Class<? extends CountersRepository>, Long>();

            for (var repo : implementations) {

                heatUp(repo, lowNames, mixture);

                val count = roll(repo, lowNames, mixture, 4, ROLL_MINUTES);

                result.put(repo.getClass(), count);
            }

            for (var resultEntry : result.entrySet()) {

                log.info("{};{}", resultEntry.getKey().getSimpleName(), resultEntry.getValue());
            }
        }
    }
}
