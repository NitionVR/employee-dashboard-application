package com.employee.exception;

public class InvalidLocationException extends RuntimeException {
    private final Long recordId;

    public InvalidLocationException(String message, Long recordId) {
        super(message);
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }
}