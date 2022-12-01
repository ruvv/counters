package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class DuplicateNameException extends ApiException {

    public DuplicateNameException(String message) {

        super(message);
    }

    public static DuplicateNameException of(String name) {

        return new DuplicateNameException(String.format("Counter with name '%s' already exists.", name));
    }

    @NonNull
    @Override
    public String getMessage() {

        return super.getMessage();
    }

    @Override
    @NonNull
    public HttpStatus getHttpStatus() {

        return HttpStatus.BAD_REQUEST;
    }
}
