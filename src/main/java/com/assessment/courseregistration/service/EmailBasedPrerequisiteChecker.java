package com.assessment.courseregistration.service;

import com.assessment.courseregistration.model.Learner;
import org.springframework.stereotype.Component;

/**
 * Email-based prerequisite checker implementation.
 * Checks if a learner's email contains "completed_" + prerequisite course code.
 * This is a simplified implementation for the bounded scope.
 * In a production system, this would query a completed courses table.
 */
@Component
public class EmailBasedPrerequisiteChecker implements PrerequisiteChecker {
    
    @Override
    public boolean meetsPrerequisite(Learner learner, String prerequisite) {
        if (learner == null || learner.getEmail() == null || prerequisite == null) {
            return false;
        }
        
        // Simplified prerequisite check: learner email contains "completed_" + prerequisite
        return learner.getEmail().contains("completed_" + prerequisite);
    }
}
