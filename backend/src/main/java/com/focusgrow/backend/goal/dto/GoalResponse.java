package com.focusgrow.backend.goal.dto;

import com.focusgrow.backend.goal.Goal;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class GoalResponse {

    private final Long goalId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer totalAmount;
    private final String unit;
    private final Integer dailyAmount;
    private final String status;

    public GoalResponse(Goal goal) {
        this.goalId = goal.getGoalId();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.totalAmount = goal.getTotalAmount();
        this.unit = goal.getUnit();
        this.dailyAmount = calculateDailyAmount(goal);
        this.status = goal.getStatus();
    }

    // 시작일~종료일(양 끝 포함) 일수로 나눠 올림
    private static int calculateDailyAmount(Goal goal) {
        long days = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate()) + 1;
        if (days <= 0) {
            return goal.getTotalAmount();
        }
        return (int) Math.ceil((double) goal.getTotalAmount() / days);
    }
}