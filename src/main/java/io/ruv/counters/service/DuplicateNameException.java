package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class DuplicateNameException extends RuntimeException implements ApiException {

    public DuplicateNameException(String message) {

        super(message);
    }

    @Override
    @NonNull
    public HttpStatus getHttpStatus() {

        return HttpStatus.BAD_REQUEST;
    }
}
