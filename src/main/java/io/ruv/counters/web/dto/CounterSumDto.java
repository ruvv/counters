package io.ruv.counters.web.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CounterSumDto {

    /**
     * Sum of counter values
     */
    private BigInteger sum;
}
