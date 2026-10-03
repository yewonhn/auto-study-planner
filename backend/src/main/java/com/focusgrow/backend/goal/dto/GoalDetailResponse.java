package com.focusgrow.backend.goal.dto;

import com.focusgrow.backend.goal.Goal;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class GoalDetailResponse {

    private final Long goalId;
    private final String title;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer totalAmount;
    private final String unit;
    private final Integer completedAmount; // TODO: 임시값 0
    private final Integer progress;        // TODO: 임시값 0
    private final Integer dailyAmount;
    private final String status;

    public GoalDetailResponse(Goal goal) {
        this.goalId = goal.getGoalId();
        this.title = goal.getTitle();
        this.description = goal.getDescription();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.totalAmount = goal.getTotalAmount();
        this.unit = goal.getUnit();
        this.completedAmount = 0;
        this.progress = 0;
        this.dailyAmount = calculateDailyAmount(goal);
        this.status = goal.getStatus();
    }

    private static int calculateDailyAmount(Goal goal) {
        long days = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate()) + 1;
        if (days <= 0) {
            return goal.getTotalAmount();
        }
        return (int) Math.ceil((double) goal.getTotalAmount() / days);
    }
}