package io.ruv.counters.util.exception;

import org.springframework.http.HttpStatus;

public interface ApiException {

    HttpStatus getHttpStatus();

    String getMessage();
}
