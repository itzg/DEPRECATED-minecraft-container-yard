package me.itzg.mccy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents an error induced by a client request, similar to an {@link java.lang.IllegalArgumentException}
 * @author Geoff Bourne
 * @since 3/13/2015
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MccyClientException extends MccyException {
    public MccyClientException() {
    }

    public MccyClientException(String message) {
        super(message);
    }

    public MccyClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MccyClientException(Throwable cause) {
        super(cause);
    }

    public MccyClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
