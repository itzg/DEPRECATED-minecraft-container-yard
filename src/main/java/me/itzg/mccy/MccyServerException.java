package me.itzg.mccy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents a failure (typically unknown or unexpected) on the server's part
 *
 * @author Geoff Bourne
 * @since 3/13/2015
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MccyServerException extends MccyException {
    public MccyServerException() {
    }

    public MccyServerException(String message) {
        super(message);
    }

    public MccyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MccyServerException(Throwable cause) {
        super(cause);
    }

    public MccyServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
