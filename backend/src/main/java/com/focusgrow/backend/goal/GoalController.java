package com.focusgrow.backend.goal;

import com.focusgrow.backend.goal.dto.GoalCreateRequest;
import com.focusgrow.backend.goal.dto.GoalDetailResponse;
import com.focusgrow.backend.goal.dto.GoalListResponse;
import com.focusgrow.backend.goal.dto.GoalResponse;
import com.focusgrow.backend.goal.dto.GoalUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // 개발 전용 사용자 헤더. 실제 인증(JWT)은 별도 구현 대상이다.
    // TODO: JWT 적용 시 토큰에서 userId를 꺼내도록 교체

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestBody GoalCreateRequest request
    ) {
        Goal goal = goalService.createGoal(
                userId,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalAmount(),
                request.getUnit()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new GoalResponse(goal));
    }

    @GetMapping
    public ResponseEntity<List<GoalListResponse>> getGoals(
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        List<GoalListResponse> goals = goalService.getGoals(userId).stream()
                .map(g -> new GoalListResponse(g, goalService.completedAmount(g.getGoalId())))
                .toList();
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponse> getGoal(
            @RequestHeader(value = "X-User-Id") Long userId,
            @PathVariable Long goalId
    ) {
        return ResponseEntity.ok(new GoalDetailResponse(goalService.getGoal(userId, goalId), goalService.completedAmount(goalId)));
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponse> updateGoal(
            @RequestHeader(value = "X-User-Id") Long userId,
            @PathVariable Long goalId,
            @RequestBody GoalUpdateRequest request
    ) {
        return ResponseEntity.ok(new GoalDetailResponse(goalService.updateGoal(userId, goalId, request), goalService.completedAmount(goalId)));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @RequestHeader(value = "X-User-Id") Long userId,
            @PathVariable Long goalId
    ) {
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
}