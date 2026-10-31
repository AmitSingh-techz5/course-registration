package com.assessment.courseregistration.repository;

import com.assessment.courseregistration.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByCourseIdAndLearnerId(Long courseId, Long learnerId);
}
