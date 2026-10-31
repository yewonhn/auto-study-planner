package com.focusgrow.backend.schedule;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;
public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select s from Schedule s where s.scheduleId=:id")
 Optional<Schedule> findLockedById(@Param("id") Long id);
 List<Schedule> findByGoal_GoalIdOrderByDateAsc(Long goalId);
 @Query("select s.goal.goalId from Schedule s where s.scheduleId=:id")
 Optional<Long> findGoalId(@Param("id") Long id);
 @Query("select s from Schedule s join fetch s.goal g where g.user.userId=:userId order by s.date,s.scheduleId")
 List<Schedule> findForUser(@Param("userId") Long userId);
 @Query("select s from Schedule s join fetch s.goal g where g.user.userId=:userId and g.status <> 'CANCELLED' and s.date=:date order by s.scheduleId")
 List<Schedule> findToday(@Param("userId") Long userId,@Param("date") LocalDate date);
}
