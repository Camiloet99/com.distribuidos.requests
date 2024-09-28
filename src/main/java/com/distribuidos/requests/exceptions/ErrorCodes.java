package com.distribuidos.requests.exceptions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {

    private static final String PREFIX = "RQ-00";

    public static final String CENTRALIZER_OPERATORS_EXCEPTION = PREFIX + "01";
    public static final String DOCUMENTS_DOWNLOAD_EXCEPTION = PREFIX + "02";
    public static final String EXTERNAL_UPSTREAM_PUSH_ERROR = PREFIX + "03";
}
