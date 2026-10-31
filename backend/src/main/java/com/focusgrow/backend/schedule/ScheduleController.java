package com.focusgrow.backend.schedule;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;
@RestController @RequestMapping("/api/schedules")
public class ScheduleController {
 private final ScheduleService service;
 private final Clock clock;
 public ScheduleController(ScheduleService service,Clock clock){this.service=service;this.clock=clock;}
 @GetMapping("/today")
 public TodayResponse today(@RequestHeader(value="X-User-Id") Long userId){
  return new TodayResponse(LocalDate.now(clock),service.today(userId).stream().map(Item::new).toList());
 }
 @PatchMapping("/{scheduleId}/complete")
 public CompleteResponse complete(@RequestHeader(value="X-User-Id") Long userId,@PathVariable Long scheduleId){
  var s=service.complete(userId,scheduleId);return new CompleteResponse(s.getScheduleId(),s.isCompleted(),s.getCompletedAt());
 }
 public record Item(Long scheduleId,Long goalId,String title,Integer amount,LocalTime startTime,LocalTime endTime,boolean completed){
  Item(Schedule s){this(s.getScheduleId(),s.getGoal().getGoalId(),s.getTitle(),s.getAmount(),s.getStartTime(),s.getEndTime(),s.isCompleted());}
 }
 public record TodayResponse(LocalDate date,List<Item> schedules){}
 public record CompleteResponse(Long scheduleId,boolean completed,LocalDateTime completedAt){}
}
