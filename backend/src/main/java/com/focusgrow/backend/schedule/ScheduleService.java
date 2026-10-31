package com.focusgrow.backend.schedule;
import com.focusgrow.backend.goal.*;
import com.focusgrow.backend.records.*;
import com.focusgrow.backend.common.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service @Transactional
public class ScheduleService {
 private final ScheduleRepository schedules;
 private final StudyRecordRepository records;
 private final GoalRepository goals;
 private final Clock clock;
 private final ScheduleCalculator calculator=new ScheduleCalculator();
 public ScheduleService(ScheduleRepository schedules,StudyRecordRepository records,GoalRepository goals,Clock clock){
  this.schedules=schedules;this.records=records;this.goals=goals;this.clock=clock;
 }
 public void generate(Goal goal){
  savePlan(goal,calculator.calculate(goal.getTotalAmount(),goal.getStartDate(),goal.getEndDate()));
 }
 // 완료 일정과 기록은 보존하고 미완료 일정만 교체한다.
 public void replan(Goal goal){
  var old=schedules.findByGoal_GoalIdOrderByDateAsc(goal.getGoalId());
  int done=old.stream().filter(Schedule::isCompleted).mapToInt(Schedule::getAmount).sum();
  if(done>goal.getTotalAmount())throw new IllegalArgumentException("총분량은 완료 분량보다 작을 수 없습니다.");
  Set<LocalDate> completedDates=new HashSet<>();
  old.stream().filter(Schedule::isCompleted).forEach(s->{
   if(s.getDate().isBefore(goal.getStartDate())||s.getDate().isAfter(goal.getEndDate()))
    throw new IllegalArgumentException("완료 일정을 제외하는 기간 변경은 할 수 없습니다.");
   completedDates.add(s.getDate());
  });
  LocalDate today=LocalDate.now(clock);
  LocalDate start=goal.getStartDate().isAfter(today)?goal.getStartDate():today;
  ScheduleCalculator.Plan plan;
  if(done==goal.getTotalAmount())plan=new ScheduleCalculator.Plan(0,0,List.of());
  else {
   if(start.isAfter(goal.getEndDate()))throw new IllegalArgumentException("남은 분량을 배정할 날짜가 없습니다.");
   try{plan=calculator.calculate(goal.getTotalAmount(),done,start,goal.getEndDate(),completedDates,null);}
   catch(IllegalStateException e){throw new IllegalArgumentException(e.getMessage());}
  }
  schedules.deleteAll(old.stream().filter(s->!s.isCompleted()).toList());
  schedules.flush(); // 동일 목표·날짜의 UNIQUE 충돌을 피한다.
  savePlan(goal,plan);
  goal.setProgressStatus(done==goal.getTotalAmount()?"COMPLETED":"IN_PROGRESS");
 }
 public void renamePending(Goal goal){
  schedules.findByGoal_GoalIdOrderByDateAsc(goal.getGoalId()).stream()
   .filter(s->!s.isCompleted()).forEach(s->s.rename(goal.getTitle()));
 }
 private void savePlan(Goal goal,ScheduleCalculator.Plan plan){
  schedules.saveAll(plan.schedules().stream().map(p->new Schedule(goal,p.date(),p.amount())).toList());
 }
 @Transactional(readOnly=true)
 public List<Schedule> today(Long userId){return schedules.findToday(userId,LocalDate.now(clock));}
 public Schedule complete(Long userId,Long scheduleId){
  Long goalId=schedules.findGoalId(scheduleId).orElseThrow(()->new NotFoundException("일정을 찾을 수 없습니다."));
  // 수정·삭제·완료가 모두 목표부터 잠그므로 중복 완료와 재계산 경합을 막는다.
  Goal goal=goals.findOwnedForUpdate(goalId,userId).orElseThrow(()->new NotFoundException("일정을 찾을 수 없습니다."));
  Schedule schedule=schedules.findLockedById(scheduleId).orElseThrow(()->new NotFoundException("일정을 찾을 수 없습니다."));
  if(schedule.isCompleted())throw new ConflictException("이미 완료된 일정입니다.");
  if(!"IN_PROGRESS".equals(goal.getStatus()))throw new ConflictException("진행 중인 목표만 완료할 수 있습니다.");
  if(schedule.getDate().isAfter(LocalDate.now(clock)))throw new ConflictException("미래 일정은 완료할 수 없습니다.");
  LocalDateTime now=LocalDateTime.now(clock);
  schedule.complete(now);records.save(new StudyRecord(schedule,now));schedules.flush();
  if(schedules.findByGoal_GoalIdOrderByDateAsc(goalId).stream().allMatch(Schedule::isCompleted))
   goal.setProgressStatus("COMPLETED");
  return schedule;
 }
 @Transactional(readOnly=true)
 public int completedAmount(Long goalId){
  return schedules.findByGoal_GoalIdOrderByDateAsc(goalId).stream().filter(Schedule::isCompleted).mapToInt(Schedule::getAmount).sum();
 }
 public void deleteForGoal(Long goalId){
  records.deleteAll(records.findBySchedule_Goal_GoalId(goalId));records.flush();
  schedules.deleteAll(schedules.findByGoal_GoalIdOrderByDateAsc(goalId));schedules.flush();
 }
}
