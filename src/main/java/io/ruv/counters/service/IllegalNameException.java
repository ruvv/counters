package io.ruv.counters.service;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class IllegalNameException extends ApiException {

    public IllegalNameException(String message) {

        super(message);
    }

    public static IllegalNameException nullName() {

        return new IllegalNameException("Counter name was not specified.");
    }

    public static IllegalNameException emptyName() {

        return new IllegalNameException("Illegal counter name '' - empty.");
    }

    public static IllegalNameException tooLongName(String name, int maxLength) {

        return new IllegalNameException(String.format("Illegal counter name '%s' - exceeds max length '%d'.",
                name, maxLength));
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
