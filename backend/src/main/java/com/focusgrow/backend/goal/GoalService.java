package com.focusgrow.backend.goal;
import com.focusgrow.backend.goal.dto.GoalUpdateRequest;
import com.focusgrow.backend.user.*;
import com.focusgrow.backend.schedule.*;
import com.focusgrow.backend.common.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Service
public class GoalService {
 private final GoalRepository goals;
 private final UserRepository users;
 private final ScheduleService schedules;
 public GoalService(GoalRepository goals,UserRepository users,ScheduleService schedules){this.goals=goals;this.users=users;this.schedules=schedules;}
 private void validate(String title,LocalDate start,LocalDate end,Integer amount,String unit){
  if(title==null||title.isBlank()||title.length()>255)throw new IllegalArgumentException("목표명은 1~255자여야 합니다.");
  if(unit==null||unit.isBlank()||unit.length()>30)throw new IllegalArgumentException("단위는 1~30자여야 합니다.");
  if(start==null||end==null||start.isAfter(end)||ChronoUnit.DAYS.between(start,end)>=3650)
   throw new IllegalArgumentException("목표 기간은 시작일~종료일 포함 1~3650일이어야 합니다.");
  if(amount==null||amount<=0)throw new IllegalArgumentException("총분량은 양수여야 합니다.");
 }
 @Transactional
 public Goal createGoal(Long userId,String title,String description,LocalDate startDate,LocalDate endDate,Integer totalAmount,String unit){
  validate(title,startDate,endDate,totalAmount,unit);
  var user=users.findById(userId).orElseThrow(()->new NotFoundException("사용자를 찾을 수 없습니다."));
  Goal goal=goals.save(new Goal(user,title.trim(),description,startDate,endDate,totalAmount,unit.trim(),"IN_PROGRESS"));
  schedules.generate(goal);return goal;
 }
 @Transactional(readOnly=true)
 public List<Goal> getGoals(Long userId){return goals.findByUser_UserIdOrderByCreatedAtDesc(userId);}
 @Transactional(readOnly=true)
 public Goal getGoal(Long userId,Long goalId){return goals.findByGoalIdAndUser_UserId(goalId,userId).orElseThrow(()->new NotFoundException("목표를 찾을 수 없습니다."));}
 private Goal lock(Long userId,Long goalId){return goals.findOwnedForUpdate(goalId,userId).orElseThrow(()->new NotFoundException("목표를 찾을 수 없습니다."));}
 @Transactional
 public Goal updateGoal(Long userId,Long goalId,GoalUpdateRequest req){
  Goal g=lock(userId,goalId);
  String title=req.getTitle()==null?g.getTitle():req.getTitle().trim();
  String unit=req.getUnit()==null?g.getUnit():req.getUnit().trim();
  LocalDate start=req.getStartDate()==null?g.getStartDate():req.getStartDate();
  LocalDate end=req.getEndDate()==null?g.getEndDate():req.getEndDate();
  Integer total=req.getTotalAmount()==null?g.getTotalAmount():req.getTotalAmount();
  validate(title,start,end,total,unit);
  if(req.getStatus()!=null&&!Set.of("IN_PROGRESS","CANCELLED").contains(req.getStatus()))
   throw new IllegalArgumentException("완료 상태는 일정 완료 결과로만 변경됩니다.");
  if(!unit.equals(g.getUnit())&&schedules.completedAmount(goalId)>0)
   throw new IllegalArgumentException("완료 기록이 있는 목표의 단위는 변경할 수 없습니다.");
  boolean replan=!start.equals(g.getStartDate())||!end.equals(g.getEndDate())||!total.equals(g.getTotalAmount())
    ||("IN_PROGRESS".equals(req.getStatus())&&!"IN_PROGRESS".equals(g.getStatus()));
  g.update(title,req.getDescription(),start,end,total,unit,req.getStatus());
  if(replan){
   if("CANCELLED".equals(g.getStatus()))throw new IllegalArgumentException("취소 목표는 재개한 뒤 기간을 변경해 주세요.");
   schedules.replan(g);
  }else schedules.renamePending(g);
  return g;
 }
 @Transactional public void replan(Long userId,Long goalId){
  Goal g=lock(userId,goalId);
  if(!"IN_PROGRESS".equals(g.getStatus()))throw new ConflictException("진행 중인 목표만 재계산할 수 있습니다.");
  schedules.replan(g);
 }
 @Transactional public void deleteGoal(Long userId,Long goalId){Goal g=lock(userId,goalId);schedules.deleteForGoal(goalId);goals.delete(g);}
 public int completedAmount(Long goalId){return schedules.completedAmount(goalId);}
}
