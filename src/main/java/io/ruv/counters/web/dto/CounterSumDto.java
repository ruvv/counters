package io.ruv.counters.web.dto;

import lombok.Data;
import lombok.NonNull;

import java.math.BigInteger;

@Data
public class CounterSumDto {

    /**
     * Sum of counter values
     */
    @NonNull
    private BigInteger sum;
}
