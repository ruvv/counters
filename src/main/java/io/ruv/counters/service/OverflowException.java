package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class OverflowException extends RuntimeException implements ApiException {

    public OverflowException(String message, Throwable cause) {

        super(message, cause);
    }

    @NonNull
    @Override
    public String getMessage() {

        return super.getMessage();
    }

    @Override
    @NonNull
    public HttpStatus getHttpStatus() {

        return HttpStatus.INSUFFICIENT_STORAGE;
    }
}
