package com.assessment.courseregistration.exception;

public class LearnerNotFoundException extends RuntimeException {
    public LearnerNotFoundException(String message) {
        super(message);
    }
}
