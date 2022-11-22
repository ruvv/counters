package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class IllegalNameException extends RuntimeException implements ApiException {

    public IllegalNameException(String message) {

        super(message);
    }

    @Override
    @NonNull
    public HttpStatus getHttpStatus() {

        return HttpStatus.BAD_REQUEST;
    }
}
