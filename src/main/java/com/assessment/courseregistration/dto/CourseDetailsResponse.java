package com.assessment.courseregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsResponse {
    private String code;
    private String name;
    private String prerequisite;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer availableSeats;
}
