package com.focusgrow.backend.schedule;
import com.focusgrow.backend.goal.GoalService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/** 원본 14개 API 밖의 명시적 재계산 확장. GET 조회는 데이터를 변경하지 않는다. */
@RestController
public class ReplanController {
 private final GoalService goals;
 public ReplanController(GoalService goals){this.goals=goals;}
 @PostMapping("/api/schedules/replan/{goalId}")
 public Map<String,String> replan(@RequestHeader("X-User-Id") Long userId,@PathVariable Long goalId){
  goals.replan(userId,goalId);return Map.of("message","미완료 일정을 재계산했습니다.");
 }
}
