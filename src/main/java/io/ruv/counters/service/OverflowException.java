package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;

public class OverflowException extends RuntimeException implements ApiException {

    public OverflowException(String message) {

        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {

        return HttpStatus.INSUFFICIENT_STORAGE;
    }
}
