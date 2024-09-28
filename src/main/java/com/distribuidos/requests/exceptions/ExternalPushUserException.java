package com.distribuidos.requests.exceptions;

import static com.distribuidos.requests.exceptions.ErrorCodes.EXTERNAL_UPSTREAM_PUSH_ERROR;

public class ExternalPushUserException extends RuntimeException {
    public ExternalPushUserException(String message) {
        super(EXTERNAL_UPSTREAM_PUSH_ERROR + " " + message);
    }
}
