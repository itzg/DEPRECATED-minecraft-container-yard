package me.itzg.mccy;

/**
 * Common base class for MCCY exceptions.
 *
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public abstract class MccyException extends Exception {
    public MccyException() {
    }

    public MccyException(String message) {
        super(message);
    }

    public MccyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MccyException(Throwable cause) {
        super(cause);
    }

    public MccyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}