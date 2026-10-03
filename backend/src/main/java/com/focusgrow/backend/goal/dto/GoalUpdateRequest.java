package com.focusgrow.backend.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalUpdateRequest {

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalAmount;
    private String unit;
    private String status;
}