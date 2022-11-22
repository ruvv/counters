package io.ruv.counters.web.dto;

import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class CounterDto {

    /**
     * Counter name
     */
    @NonNull
    private String name;

    /**
     * Counter value
     */
    private long value;
}
