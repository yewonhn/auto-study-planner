package com.focusgrow.backend.goal;

import com.focusgrow.backend.goal.dto.GoalUpdateRequest;
import com.focusgrow.backend.user.User;
import com.focusgrow.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.focusgrow.backend.common.NotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(
            GoalRepository goalRepository,
            UserRepository userRepository
    ) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Goal createGoal(
            Long userId,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Integer totalAmount,
            String unit
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("사용자를 찾을 수 없습니다.")
                );

        Goal goal = new Goal(
                user,
                title,
                description,
                startDate,
                endDate,
                totalAmount,
                unit,
                "IN_PROGRESS"
        );

        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public List<Goal> getGoals(Long userId) {
        return goalRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Goal getGoal(Long userId, Long goalId) {
        return goalRepository.findByGoalIdAndUser_UserId(goalId, userId)
                .orElseThrow(() ->
                        new NotFoundException("목표를 찾을 수 없습니다.")
                );
    }

    @Transactional
    public Goal updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {
        Goal goal = getGoal(userId, goalId);
        goal.update(
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalAmount(),
                request.getUnit(),
                request.getStatus()
        );
        return goal;
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = getGoal(userId, goalId);
        goalRepository.delete(goal);
    }
}