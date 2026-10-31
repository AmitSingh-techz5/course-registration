package com.assessment.courseregistration.exception;

public class AlreadyOnWaitlistException extends RuntimeException {
    public AlreadyOnWaitlistException(String message) {
        super(message);
    }
}
