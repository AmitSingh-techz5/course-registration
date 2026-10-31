package com.assessment.courseregistration.repository;

import com.assessment.courseregistration.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    Optional<WaitlistEntry> findByCourseIdAndLearnerId(Long courseId, Long learnerId);
    
    List<WaitlistEntry> findByCourseIdOrderByPositionAsc(Long courseId);
    
    @Query("SELECT MAX(w.position) FROM WaitlistEntry w WHERE w.course.id = :courseId")
    Integer findMaxPositionByCourseId(Long courseId);
}
