package io.ruv.counters.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public interface ApiException {

    @NonNull
    HttpStatus getHttpStatus();

    @NonNull
    String getMessage();
}
