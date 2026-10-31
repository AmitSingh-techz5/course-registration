package com.assessment.courseregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistResponse {
    private String courseCode;
    private List<WaitlistEntryDto> waitlist;
}
