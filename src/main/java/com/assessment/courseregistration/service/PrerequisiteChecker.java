package com.assessment.courseregistration.service;

import com.assessment.courseregistration.model.Learner;

/**
 * Strategy interface for checking if a learner meets course prerequisites.
 * This follows the Open/Closed Principle - new prerequisite checking strategies
 * can be added without modifying existing code.
 */
public interface PrerequisiteChecker {
    
    /**
     * Checks if a learner meets the specified prerequisite.
     * 
     * @param learner the learner to check
     * @param prerequisite the prerequisite course code
     * @return true if the learner meets the prerequisite, false otherwise
     */
    boolean meetsPrerequisite(Learner learner, String prerequisite);
}
