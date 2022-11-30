package io.ruv.counters.repo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SimpleCounter implements Counter {

    private final String name;
    private final long value;
}
