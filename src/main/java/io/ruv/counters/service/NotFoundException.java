package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {

        super(message);
    }

    public static NotFoundException of(String name) {

        return new NotFoundException(String.format("Counter with name '%s' does not exist.", name));
    }

    @NonNull
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    @NonNull
    public HttpStatus getHttpStatus() {

        return HttpStatus.NOT_FOUND;
    }
}
