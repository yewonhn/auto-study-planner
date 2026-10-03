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

    // 임시: JWT 적용 전까지 X-User-Id 헤더로 사용자 구분 (없으면 1)
    // TODO: JWT 적용 시 토큰에서 userId를 꺼내도록 교체

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
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
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        List<GoalListResponse> goals = goalService.getGoals(userId).stream()
                .map(GoalListResponse::new)
                .toList();
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponse> getGoal(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long goalId
    ) {
        return ResponseEntity.ok(new GoalDetailResponse(goalService.getGoal(userId, goalId)));
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponse> updateGoal(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long goalId,
            @RequestBody GoalUpdateRequest request
    ) {
        return ResponseEntity.ok(new GoalDetailResponse(goalService.updateGoal(userId, goalId, request)));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long goalId
    ) {
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
}