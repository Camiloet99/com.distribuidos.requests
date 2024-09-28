package com.distribuidos.requests.exceptions;

import static com.distribuidos.requests.exceptions.ErrorCodes.DOCUMENTS_DOWNLOAD_EXCEPTION;

public class DocumentsDownloadUpstreamException extends RuntimeException {
    public DocumentsDownloadUpstreamException(String message) {
        super(DOCUMENTS_DOWNLOAD_EXCEPTION + " " + message);
    }
}
