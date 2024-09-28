package com.distribuidos.requests.exceptions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {

    private static final String PREFIX = "RQ-00";

    public static final String CENTRALIZER_OPERATORS_EXCEPTION = PREFIX + "01";
}
