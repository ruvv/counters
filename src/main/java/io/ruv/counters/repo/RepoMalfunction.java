package io.ruv.counters.repo;

import io.ruv.counters.util.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RepoMalfunction extends RuntimeException implements ApiException {

    public RepoMalfunction(String message) {

        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
