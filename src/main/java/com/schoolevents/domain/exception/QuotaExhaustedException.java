package com.schoolevents.domain.exception;

public class QuotaExhaustedException extends RuntimeException {
    public QuotaExhaustedException(String message) {
        super(message);
    }
}
