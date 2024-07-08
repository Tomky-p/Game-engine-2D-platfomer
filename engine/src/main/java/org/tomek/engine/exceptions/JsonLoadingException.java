package org.tomek.engine.exceptions;

import java.io.IOException;

public class JsonLoadingException extends IOException {
    public JsonLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
