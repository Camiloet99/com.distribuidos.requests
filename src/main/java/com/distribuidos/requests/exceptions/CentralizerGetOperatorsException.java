package com.distribuidos.requests.exceptions;

import static com.distribuidos.requests.exceptions.ErrorCodes.CENTRALIZER_OPERATORS_EXCEPTION;

public class CentralizerGetOperatorsException extends RuntimeException {
    public CentralizerGetOperatorsException(String message) {
        super(CENTRALIZER_OPERATORS_EXCEPTION + " " + message);
    }
}
