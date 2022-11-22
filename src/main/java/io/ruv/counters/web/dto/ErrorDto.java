package io.ruv.counters.web.dto;

import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * Error details container
 */
@Data
public class ErrorDto {

    /**
     * Error details
     */
    @NonNull
    private String message;
}
