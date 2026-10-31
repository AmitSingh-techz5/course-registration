package com.assessment.courseregistration.controller;

import com.assessment.courseregistration.dto.*;
import com.assessment.courseregistration.service.CourseRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class CourseRegistrationController {
    
    private final CourseRegistrationService courseRegistrationService;
    
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = courseRegistrationService.enroll(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/drop")
    public ResponseEntity<EnrollmentResponse> drop(@RequestBody DropRequest request) {
        EnrollmentResponse response = courseRegistrationService.drop(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus(
            @RequestParam String courseCode,
            @RequestParam String learnerEmail) {
        StatusResponse response = courseRegistrationService.getStatus(courseCode, learnerEmail);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/course/{code}")
    public ResponseEntity<CourseDetailsResponse> getCourseDetails(@PathVariable String code) {
        CourseDetailsResponse response = courseRegistrationService.getCourseDetails(code);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/waitlist/{courseCode}")
    public ResponseEntity<WaitlistResponse> getWaitlist(@PathVariable String courseCode) {
        WaitlistResponse response = courseRegistrationService.getWaitlist(courseCode);
        return ResponseEntity.ok(response);
    }
}
