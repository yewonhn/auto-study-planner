package com.focusgrow.backend.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    Optional<Goal> findByGoalIdAndUser_UserId(Long goalId, Long userId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select g from Goal g where g.goalId=:goalId and g.user.userId=:userId")
    java.util.Optional<Goal> findOwnedForUpdate(@org.springframework.data.repository.query.Param("goalId") Long goalId,
      @org.springframework.data.repository.query.Param("userId") Long userId);
}