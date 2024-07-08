package org.tomek.engine.exceptions;

import java.io.IOException;

public class JsonSavingException extends IOException {
    public JsonSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}
