package io.ruv.counters.web.dto;

import lombok.Data;

@Data
public class CounterDto {

    /**
     * Counter name
     */
    private String name;

    /**
     * Counter value
     */
    private long value;
}
