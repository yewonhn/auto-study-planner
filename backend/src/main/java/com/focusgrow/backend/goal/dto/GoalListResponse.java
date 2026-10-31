package com.focusgrow.backend.goal.dto;

import com.focusgrow.backend.goal.Goal;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GoalListResponse {

    private final Long goalId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer progress;
    private final String status;

    public GoalListResponse(Goal goal) { this(goal, 0); }

    public GoalListResponse(Goal goal, int completedAmount) {
        this.goalId = goal.getGoalId();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.progress = (int) ((long) completedAmount * 100 / goal.getTotalAmount());
        this.status = goal.getStatus();
    }
}