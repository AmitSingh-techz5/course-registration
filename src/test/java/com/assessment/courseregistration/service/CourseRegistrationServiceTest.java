package com.assessment.courseregistration.service;

import com.assessment.courseregistration.dto.*;
import com.assessment.courseregistration.exception.*;
import com.assessment.courseregistration.model.Course;
import com.assessment.courseregistration.model.Enrollment;
import com.assessment.courseregistration.model.Learner;
import com.assessment.courseregistration.model.WaitlistEntry;
import com.assessment.courseregistration.repository.CourseRepository;
import com.assessment.courseregistration.repository.EnrollmentRepository;
import com.assessment.courseregistration.repository.LearnerRepository;
import com.assessment.courseregistration.repository.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CourseRegistrationServiceTest {

    @Autowired
    private CourseRegistrationService courseRegistrationService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    private Course course;
    private Learner eligibleLearner1;
    private Learner eligibleLearner2;
    private Learner eligibleLearner3;
    private Learner eligibleLearner4;
    private Learner eligibleLearner5;
    private Learner ineligibleLearner;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        waitlistEntryRepository.deleteAll();
        learnerRepository.deleteAll();
        courseRepository.deleteAll();

        course = new Course();
        course.setCode("CS201");
        course.setName("Advanced Programming");
        course.setPrerequisite("CS101");
        course.setCapacity(3);
        course.setEnrolledCount(0);
        course = courseRepository.save(course);

        eligibleLearner1 = new Learner();
        eligibleLearner1.setEmail("learner1_completed_CS101@example.com");
        eligibleLearner1.setName("Learner One");
        eligibleLearner1 = learnerRepository.save(eligibleLearner1);

        eligibleLearner2 = new Learner();
        eligibleLearner2.setEmail("learner2_completed_CS101@example.com");
        eligibleLearner2.setName("Learner Two");
        eligibleLearner2 = learnerRepository.save(eligibleLearner2);

        eligibleLearner3 = new Learner();
        eligibleLearner3.setEmail("learner3_completed_CS101@example.com");
        eligibleLearner3.setName("Learner Three");
        eligibleLearner3 = learnerRepository.save(eligibleLearner3);

        eligibleLearner4 = new Learner();
        eligibleLearner4.setEmail("learner4_completed_CS101@example.com");
        eligibleLearner4.setName("Learner Four");
        eligibleLearner4 = learnerRepository.save(eligibleLearner4);

        eligibleLearner5 = new Learner();
        eligibleLearner5.setEmail("learner6_completed_CS101@example.com");
        eligibleLearner5.setName("Learner Six");
        eligibleLearner5 = learnerRepository.save(eligibleLearner5);

        ineligibleLearner = new Learner();
        ineligibleLearner.setEmail("learner5_no_prereq@example.com");
        ineligibleLearner.setName("Learner Five");
        ineligibleLearner = learnerRepository.save(ineligibleLearner);
    }

    @Test
    void testEnroll_WhenCapacityAvailable_ShouldEnrollSuccessfully() {
        EnrollmentRequest request = new EnrollmentRequest("CS201", eligibleLearner1.getEmail());

        EnrollmentResponse response = courseRegistrationService.enroll(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Enrolled successfully", response.getMessage());
        assertNull(response.getWaitlistPosition());

        Course updatedCourse = courseRepository.findByCode("CS201").orElseThrow();
        assertEquals(1, updatedCourse.getEnrolledCount());

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertEquals(1, enrollments.size());
    }

    @Test
    void testEnroll_WhenPrerequisiteNotMet_ShouldFail() {
        EnrollmentRequest request = new EnrollmentRequest("CS201", ineligibleLearner.getEmail());

        assertThrows(PrerequisiteNotMetException.class, () -> courseRegistrationService.enroll(request));

        Course updatedCourse = courseRepository.findByCode("CS201").orElseThrow();
        assertEquals(0, updatedCourse.getEnrolledCount());

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertEquals(0, enrollments.size());
    }

    @Test
    void testEnroll_WhenFull_ShouldAddToWaitlist() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Try to enroll when full
        EnrollmentRequest request = new EnrollmentRequest("CS201", eligibleLearner4.getEmail());
        EnrollmentResponse response = courseRegistrationService.enroll(request);

        assertEquals("WAITLISTED", response.getStatus());
        assertEquals("Added to waitlist", response.getMessage());
        assertEquals(1, response.getWaitlistPosition());

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(1, waitlist.size());
    }

    @Test
    void testEnroll_DuplicateEnrollment_ShouldFail() {
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));

        EnrollmentRequest request = new EnrollmentRequest("CS201", eligibleLearner1.getEmail());
        assertThrows(AlreadyEnrolledException.class, () -> courseRegistrationService.enroll(request));

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertEquals(1, enrollments.size());
    }

    @Test
    void testEnroll_DuplicateWaitlistRequest_ShouldFail() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Add to waitlist
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner4.getEmail()));

        // Try to add to waitlist again
        EnrollmentRequest request = new EnrollmentRequest("CS201", eligibleLearner4.getEmail());
        assertThrows(AlreadyOnWaitlistException.class, () -> courseRegistrationService.enroll(request));

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(1, waitlist.size());
    }

    @Test
    void testEnroll_IneligibleLearnerWhenFull_ShouldNotAddToWaitlist() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Try to enroll ineligible learner when full
        EnrollmentRequest request = new EnrollmentRequest("CS201", ineligibleLearner.getEmail());
        assertThrows(PrerequisiteNotMetException.class, () -> courseRegistrationService.enroll(request));

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(0, waitlist.size());
    }

    @Test
    void testDrop_WhenEnrolled_ShouldDropAndPromoteFromWaitlist() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Add to waitlist
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner4.getEmail()));

        // Drop first learner
        DropRequest request = new DropRequest("CS201", eligibleLearner1.getEmail());
        EnrollmentResponse response = courseRegistrationService.drop(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Dropped successfully", response.getMessage());

        Course updatedCourse = courseRepository.findByCode("CS201").orElseThrow();
        assertEquals(3, updatedCourse.getEnrolledCount());

        // Check that waitlisted learner was promoted
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertEquals(3, enrollments.size());
        assertTrue(enrollments.stream().anyMatch(e -> e.getLearner().getEmail().equals(eligibleLearner4.getEmail())));

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(0, waitlist.size());
    }

    @Test
    void testDrop_WhenWaitlistHasIneligibleLearner_ShouldSkipAndPromoteNext() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Add ineligible learner to waitlist (manually since enrollment would fail)
        WaitlistEntry ineligibleEntry = new WaitlistEntry();
        ineligibleEntry.setCourse(course);
        ineligibleEntry.setLearner(ineligibleLearner);
        ineligibleEntry.setJoinedAt(System.currentTimeMillis());
        ineligibleEntry.setPosition(1);
        waitlistEntryRepository.save(ineligibleEntry);

        // Add eligible learner to waitlist
        WaitlistEntry eligibleEntry = new WaitlistEntry();
        eligibleEntry.setCourse(course);
        eligibleEntry.setLearner(eligibleLearner4);
        eligibleEntry.setJoinedAt(System.currentTimeMillis());
        eligibleEntry.setPosition(2);
        waitlistEntryRepository.save(eligibleEntry);

        // Drop first learner
        DropRequest request = new DropRequest("CS201", eligibleLearner1.getEmail());
        EnrollmentResponse response = courseRegistrationService.drop(request);

        assertEquals("SUCCESS", response.getStatus());

        Course updatedCourse = courseRepository.findByCode("CS201").orElseThrow();
        assertEquals(3, updatedCourse.getEnrolledCount());

        // Check that ineligible learner was skipped and eligible was promoted
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertEquals(3, enrollments.size());
        assertTrue(enrollments.stream().anyMatch(e -> e.getLearner().getEmail().equals(eligibleLearner4.getEmail())));
        assertFalse(enrollments.stream().anyMatch(e -> e.getLearner().getEmail().equals(ineligibleLearner.getEmail())));

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(0, waitlist.size());
    }

    @Test
    void testDrop_WhenNotEnrolled_ShouldFail() {
        DropRequest request = new DropRequest("CS201", eligibleLearner1.getEmail());
        assertThrows(NotEnrolledException.class, () -> courseRegistrationService.drop(request));
    }

    @Test
    void testDrop_DuplicateDrop_ShouldFail() {
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));

        DropRequest request = new DropRequest("CS201", eligibleLearner1.getEmail());
        courseRegistrationService.drop(request);

        assertThrows(NotEnrolledException.class, () -> courseRegistrationService.drop(request));
    }

    @Test
    void testFIFO_WaitlistOrder() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));

        // Add multiple learners to waitlist
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner4.getEmail()));

        // Create another eligible learner
        Learner eligibleLearner5 = new Learner();
        eligibleLearner5.setEmail("learner6_completed_CS101@example.com");
        eligibleLearner5.setName("Learner Six");
        eligibleLearner5 = learnerRepository.save(eligibleLearner5);

        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner5.getEmail()));

        List<WaitlistEntry> waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(2, waitlist.size());
        assertEquals(eligibleLearner4.getEmail(), waitlist.get(0).getLearner().getEmail());
        assertEquals(eligibleLearner5.getEmail(), waitlist.get(1).getLearner().getEmail());

        // Drop first enrolled learner - should promote first waitlisted
        courseRegistrationService.drop(new DropRequest("CS201", eligibleLearner1.getEmail()));

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertTrue(enrollments.stream().anyMatch(e -> e.getLearner().getEmail().equals(eligibleLearner4.getEmail())));

        // Check remaining waitlist
        waitlist = waitlistEntryRepository.findByCourseIdOrderByPositionAsc(course.getId());
        assertEquals(1, waitlist.size());
        assertEquals(eligibleLearner5.getEmail(), waitlist.get(0).getLearner().getEmail());
    }

    @Test
    void testEnroll_CourseNotFound_ShouldFail() {
        EnrollmentRequest request = new EnrollmentRequest("NONEXISTENT", eligibleLearner1.getEmail());

        assertThrows(CourseNotFoundException.class, () -> courseRegistrationService.enroll(request));
    }

    @Test
    void testEnroll_LearnerNotFound_ShouldFail() {
        EnrollmentRequest request = new EnrollmentRequest("CS201", "nonexistent@example.com");

        assertThrows(LearnerNotFoundException.class, () -> courseRegistrationService.enroll(request));
    }

    @Test
    void testDrop_CourseNotFound_ShouldFail() {
        DropRequest request = new DropRequest("NONEXISTENT", eligibleLearner1.getEmail());

        assertThrows(CourseNotFoundException.class, () -> courseRegistrationService.drop(request));
    }

    @Test
    void testDrop_LearnerNotFound_ShouldFail() {
        DropRequest request = new DropRequest("CS201", "nonexistent@example.com");

        assertThrows(LearnerNotFoundException.class, () -> courseRegistrationService.drop(request));
    }
    
    @Test
    void testGetStatus_WhenEnrolled_ShouldReturnEnrolledStatus() {
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        
        StatusResponse response = courseRegistrationService.getStatus("CS201", eligibleLearner1.getEmail());
        
        assertEquals("ENROLLED", response.getStatus());
        assertEquals("Learner is enrolled in this course", response.getMessage());
        assertNull(response.getWaitlistPosition());
    }
    
    @Test
    void testGetStatus_WhenOnWaitlist_ShouldReturnWaitlistedStatus() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));
        
        // Add to waitlist
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner4.getEmail()));
        
        StatusResponse response = courseRegistrationService.getStatus("CS201", eligibleLearner4.getEmail());
        
        assertEquals("WAITLISTED", response.getStatus());
        assertEquals("Learner is on waitlist for this course", response.getMessage());
        assertEquals(1, response.getWaitlistPosition());
    }
    
    @Test
    void testGetStatus_WhenNotEnrolled_ShouldReturnNotEnrolledStatus() {
        StatusResponse response = courseRegistrationService.getStatus("CS201", eligibleLearner1.getEmail());
        
        assertEquals("NOT_ENROLLED", response.getStatus());
        assertEquals("Learner is not enrolled in this course", response.getMessage());
        assertNull(response.getWaitlistPosition());
    }
    
    @Test
    void testGetCourseDetails_ShouldReturnCourseDetails() {
        CourseDetailsResponse response = courseRegistrationService.getCourseDetails("CS201");
        
        assertEquals("CS201", response.getCode());
        assertEquals("Advanced Programming", response.getName());
        assertEquals("CS101", response.getPrerequisite());
        assertEquals(3, response.getCapacity());
        assertEquals(0, response.getEnrolledCount());
        assertEquals(3, response.getAvailableSeats());
    }
    
    @Test
    void testGetCourseDetails_WithEnrollments_ShouldReturnCorrectCounts() {
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        
        CourseDetailsResponse response = courseRegistrationService.getCourseDetails("CS201");
        
        assertEquals(2, response.getEnrolledCount());
        assertEquals(1, response.getAvailableSeats());
    }
    
    @Test
    void testGetWaitlist_WhenEmpty_ShouldReturnEmptyList() {
        WaitlistResponse response = courseRegistrationService.getWaitlist("CS201");
        
        assertEquals("CS201", response.getCourseCode());
        assertTrue(response.getWaitlist().isEmpty());
    }
    
    @Test
    void testGetWaitlist_WithEntries_ShouldReturnWaitlist() {
        // Fill capacity
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner1.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner2.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner3.getEmail()));
        
        // Add to waitlist
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner4.getEmail()));
        courseRegistrationService.enroll(new EnrollmentRequest("CS201", eligibleLearner5.getEmail()));
        
        WaitlistResponse response = courseRegistrationService.getWaitlist("CS201");
        
        assertEquals("CS201", response.getCourseCode());
        assertEquals(2, response.getWaitlist().size());
        assertEquals(eligibleLearner4.getEmail(), response.getWaitlist().get(0).getLearnerEmail());
        assertEquals(1, response.getWaitlist().get(0).getPosition());
        assertEquals(eligibleLearner5.getEmail(), response.getWaitlist().get(1).getLearnerEmail());
        assertEquals(2, response.getWaitlist().get(1).getPosition());
    }
}
