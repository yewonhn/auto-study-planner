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
    private final Integer progress; // TODO: SCHEDULE 구현 후 실제 계산 (현재 임시값 0)
    private final String status;

    public GoalListResponse(Goal goal) {
        this.goalId = goal.getGoalId();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.progress = 0;
        this.status = goal.getStatus();
    }
}