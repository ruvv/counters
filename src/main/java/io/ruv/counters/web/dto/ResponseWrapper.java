package io.ruv.counters.web.dto;

import io.ruv.counters.util.exception.ApiException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Clock;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class ResponseWrapper<T> {

    /**
     * Response timestamp
     */
    @NonNull
    private ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());

    /**
     * Response http status
     */
    @NonNull
    private HttpStatus httpStatus = HttpStatus.OK;

    /**
     * Optional error details
     */
    @Nullable
    private ErrorDto error;

    /**
     * Optional response payload
     */
    @Nullable
    private T data;


    public static <T> ResponseWrapper<T> of(T data) {

        return new ResponseWrapper<T>()
                .setTimestamp(ZonedDateTime.now(Clock.systemUTC()))
                .setHttpStatus(HttpStatus.OK)
                .setData(data);
    }

    public static ResponseWrapper<?> of(ApiException exception) {

        return new ResponseWrapper<Object>()
                .setTimestamp(ZonedDateTime.now(Clock.systemUTC()))
                .setHttpStatus(exception.getHttpStatus())
                .setError(new ErrorDto(exception.getMessage()));
    }
}
