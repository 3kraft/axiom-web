package org.zalando.axiom.web.exceptions;

public class LoadException extends RuntimeException {

    public LoadException() {
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }

}
