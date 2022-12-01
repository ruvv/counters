package io.ruv.counters.web.exception;

import io.ruv.counters.util.exception.ApiException;
import io.ruv.counters.web.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class CountersExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ResponseWrapper<?>> handleNotFound(NoHandlerFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.of(e));
    }

    @ExceptionHandler
    public ResponseEntity<ResponseWrapper<?>> handleApiException(ApiException e) {

        return ResponseEntity.status(e.getHttpStatus())
                .body(ResponseWrapper.of(e));
    }

    @ExceptionHandler
    public ResponseEntity<ResponseWrapper<?>> handleGeneralException(Exception e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.unhandled(e));
    }
}
