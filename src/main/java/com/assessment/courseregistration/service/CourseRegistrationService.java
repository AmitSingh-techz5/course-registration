package com.assessment.courseregistration.service;

import com.assessment.courseregistration.dto.*;
import com.assessment.courseregistration.exception.*;
import com.assessment.courseregistration.model.*;
import com.assessment.courseregistration.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRegistrationService {
    
    private final CourseRepository courseRepository;
    private final LearnerRepository learnerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final PrerequisiteChecker prerequisiteChecker;
    
    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Course course = courseRepository.findByCode(request.getCourseCode())
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + request.getCourseCode()));
        
        Learner learner = learnerRepository.findByEmail(request.getLearnerEmail())
                .orElseThrow(() -> new LearnerNotFoundException("Learner not found: " + request.getLearnerEmail()));
        
        // Check if already enrolled
        if (enrollmentRepository.findByCourseIdAndLearnerId(course.getId(), learner.getId()).isPresent()) {
            throw new AlreadyEnrolledException("Already enrolled in this course");
        }
        
        // Check if already on waitlist
        if (waitlistEntryRepository.findByCourseIdAndLearnerId(course.getId(), learner.getId()).isPresent()) {
            throw new AlreadyOnWaitlistException("Already on waitlist for this course");
        }
        
        // Check prerequisite
        if (!prerequisiteChecker.meetsPrerequisite(learner, course.getPrerequisite())) {
            throw new PrerequisiteNotMetException("Prerequisite not met: " + course.getPrerequisite());
        }
        
        // Check capacity
        if (course.getEnrolledCount() < course.getCapacity()) {
            // Enroll directly
            Enrollment enrollment = new Enrollment();
            enrollment.setCourse(course);
            enrollment.setLearner(learner);
            enrollment.setEnrolledAt(System.currentTimeMillis());
            enrollmentRepository.save(enrollment);
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            courseRepository.save(course);
            return new EnrollmentResponse("SUCCESS", "Enrolled successfully", null);
        } else {
            // Add to waitlist
            Integer maxPosition = waitlistEntryRepository.findMaxPositionByCourseId(course.getId());
            int newPosition = (maxPosition == null) ? 1 : maxPosition + 1;
            WaitlistEntry entry = new WaitlistEntry();
            entry.setCourse(course);
            entry.setLearner(learner);
            entry.setJoinedAt(System.currentTimeMillis());
            entry.setPosition(newPosition);
            waitlistEntryRepository.save(entry);
            return new EnrollmentResponse("WAITLISTED", "Added to waitlist", newPosition);
        }
    }
    
    @Transactional
    public EnrollmentResponse drop(DropRequest request) {
        Course course = courseRepository.findByCode(request.getCourseCode())
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + request.getCourseCode()));
        
        Learner learner = learnerRepository.findByEmail(request.getLearnerEmail())
                .orElseThrow(() -> new LearnerNotFoundException("Learner not found: " + request.getLearnerEmail()));
        
        // Check if enrolled
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndLearnerId(course.getId(), learner.getId())
                .orElse(null);
        
        if (enrollment == null) {
            throw new NotEnrolledException("Not enrolled in this course");
        }
        
        // Remove enrollment
        enrollmentRepository.delete(enrollment);
        course.setEnrolledCount(course.getEnrolledCount() - 1);
        courseRepository.save(course);
        
        // Promote from waitlist
        promoteFromWaitlist(course);
        
        return new EnrollmentResponse("SUCCESS", "Dropped successfully", null);
    }
    
    private void promoteFromWaitlist(Course course) {
        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        
        for (WaitlistEntry entry : waitlist) {
            if (prerequisiteChecker.meetsPrerequisite(entry.getLearner(), course.getPrerequisite())) {
                // Promote this learner
                Enrollment enrollment = new Enrollment();
                enrollment.setCourse(course);
                enrollment.setLearner(entry.getLearner());
                enrollment.setEnrolledAt(System.currentTimeMillis());
                enrollmentRepository.save(enrollment);
                course.setEnrolledCount(course.getEnrolledCount() + 1);
                courseRepository.save(course);
                
                // Remove from waitlist
                waitlistEntryRepository.delete(entry);
                
                // Reorder remaining waitlist entries
                reorderWaitlist(course);
                return;
            } else {
                // Ineligible learner - skip and remove from waitlist
                waitlistEntryRepository.delete(entry);
                reorderWaitlist(course);
            }
        }
    }
    
    private void reorderWaitlist(Course course) {
        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        int position = 1;
        for (WaitlistEntry entry : waitlist) {
            entry.setPosition(position);
            waitlistEntryRepository.save(entry);
            position++;
        }
    }
    
    public StatusResponse getStatus(String courseCode, String learnerEmail) {
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + courseCode));
        
        Learner learner = learnerRepository.findByEmail(learnerEmail)
                .orElseThrow(() -> new LearnerNotFoundException("Learner not found: " + learnerEmail));
        
        // Check if enrolled
        if (enrollmentRepository.findByCourseIdAndLearnerId(course.getId(), learner.getId()).isPresent()) {
            return new StatusResponse("ENROLLED", "Learner is enrolled in this course", null);
        }
        
        // Check if on waitlist
        WaitlistEntry waitlistEntry = waitlistEntryRepository.findByCourseIdAndLearnerId(course.getId(), learner.getId())
                .orElse(null);
        
        if (waitlistEntry != null) {
            return new StatusResponse("WAITLISTED", "Learner is on waitlist for this course", waitlistEntry.getPosition());
        }
        
        return new StatusResponse("NOT_ENROLLED", "Learner is not enrolled in this course", null);
    }
    
    public CourseDetailsResponse getCourseDetails(String courseCode) {
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + courseCode));
        
        int availableSeats = course.getCapacity() - course.getEnrolledCount();
        
        return new CourseDetailsResponse(
            course.getCode(),
            course.getName(),
            course.getPrerequisite(),
            course.getCapacity(),
            course.getEnrolledCount(),
            availableSeats
        );
    }
    
    public WaitlistResponse getWaitlist(String courseCode) {
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + courseCode));
        
        List<WaitlistEntry> waitlistEntries = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        
        List<WaitlistEntryDto> waitlistDtos = waitlistEntries.stream()
                .map(entry -> new WaitlistEntryDto(entry.getLearner().getEmail(), entry.getPosition()))
                .toList();
        
        return new WaitlistResponse(courseCode, waitlistDtos);
    }
}
