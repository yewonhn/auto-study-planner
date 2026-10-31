package com.focusgrow.backend.records;
import com.focusgrow.backend.goal.GoalRepository;
import com.focusgrow.backend.schedule.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
@RestController @RequestMapping("/api/records") @Transactional(readOnly=true)
public class RecordController {
 private final ScheduleRepository schedules;
 private final StudyRecordRepository records;
 private final GoalRepository goals;
 public RecordController(ScheduleRepository schedules,StudyRecordRepository records,GoalRepository goals){this.schedules=schedules;this.records=records;this.goals=goals;}
 @GetMapping public RecordSummaryResponse getRecords(@RequestHeader("X-User-Id") Long userId){
  var all=schedules.findForUser(userId);var mine=goals.findByUser_UserIdOrderByCreatedAtDesc(userId);
  var done=records.findByUser_UserId(userId);
  int time=done.stream().mapToInt(r->r.getStudyTime()==null?0:r.getStudyTime()).sum();
  List<DailyRecord> daily=all.stream().collect(Collectors.groupingBy(Schedule::getDate,TreeMap::new,Collectors.toList())).entrySet().stream().map(e->{
   int study=done.stream().filter(r->r.getCompletedAt().toLocalDate().equals(e.getKey())).mapToInt(r->r.getStudyTime()==null?0:r.getStudyTime()).sum();
   return new DailyRecord(e.getKey().toString(),(int)e.getValue().stream().filter(Schedule::isCompleted).count(),e.getValue().size(),study);
  }).toList();
  return new RecordSummaryResponse(mine.size(),(int)mine.stream().filter(g->"COMPLETED".equals(g.getStatus())).count(),time,rate(all),daily);
 }
 @GetMapping("/monthly") public MonthlyRecordResponse getMonthly(@RequestHeader("X-User-Id") Long userId,@RequestParam int year,@RequestParam int month){
  if(year<1||year>9999||month<1||month>12)throw new IllegalArgumentException("연도와 월 범위를 확인해 주세요.");
  YearMonth ym=YearMonth.of(year,month);
  var items=schedules.findForUser(userId).stream().filter(s->YearMonth.from(s.getDate()).equals(ym))
   .collect(Collectors.groupingBy(Schedule::getDate,TreeMap::new,Collectors.toList())).entrySet().stream()
   .map(e->new MonthlyItem(e.getKey().toString(),rate(e.getValue()))).toList();
  return new MonthlyRecordResponse(year,month,items);
 }
 private int rate(List<Schedule> list){return list.isEmpty()?0:(int)(list.stream().filter(Schedule::isCompleted).count()*100/list.size());}
 public record DailyRecord(String date,Integer completedCount,Integer totalCount,Integer studyTime){}
 public record RecordSummaryResponse(Integer totalGoals,Integer completedGoals,Integer totalStudyTime,Integer completionRate,List<DailyRecord> records){}
 public record MonthlyItem(String date,Integer completionRate){}
 public record MonthlyRecordResponse(Integer year,Integer month,List<MonthlyItem> records){}
}
