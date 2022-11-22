package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends RuntimeException implements ApiException {

    public NotFoundException(String message) {

        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {

        return HttpStatus.NOT_FOUND;
    }
}
