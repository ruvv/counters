package io.ruv.counters.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public abstract class ApiException extends RuntimeException {

    public ApiException(String message) {

        super(message);
    }

    public ApiException(String message, Throwable cause) {

        super(message, cause);
    }

    @NonNull
    public abstract HttpStatus getHttpStatus();

    @NonNull
    @Override
    public String getMessage() {

        return super.getMessage();
    }
}
