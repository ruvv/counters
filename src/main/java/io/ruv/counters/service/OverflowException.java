package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class OverflowException extends ApiException {

    public OverflowException(String message, Throwable cause) {

        super(message, cause);
    }

    public static OverflowException of(String name, Throwable cause) {

        return new OverflowException(String.format("Counter '%s' can not be incremented without overflowing."
                , name), cause);
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
