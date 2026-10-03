package com.focusgrow.backend.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    Optional<Goal> findByGoalIdAndUser_UserId(Long goalId, Long userId);
}