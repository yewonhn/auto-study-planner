package com.focusgrow.backend.common;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}