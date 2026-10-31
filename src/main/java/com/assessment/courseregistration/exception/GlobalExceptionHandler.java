package com.assessment.courseregistration.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(CourseNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "COURSE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(LearnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLearnerNotFound(LearnerNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "LEARNER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(AlreadyEnrolledException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyEnrolled(AlreadyEnrolledException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "ALREADY_ENROLLED", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(AlreadyOnWaitlistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyOnWaitlist(AlreadyOnWaitlistException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "ALREADY_ON_WAITLIST", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(PrerequisiteNotMetException.class)
    public ResponseEntity<ErrorResponse> handlePrerequisiteNotMet(PrerequisiteNotMetException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "PREREQUISITE_NOT_MET", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(NotEnrolledException.class)
    public ResponseEntity<ErrorResponse> handleNotEnrolled(NotEnrolledException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "NOT_ENROLLED", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse("ERROR", "INVALID_REQUEST", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        ErrorResponse error = new ErrorResponse("ERROR", "INTERNAL_ERROR", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
