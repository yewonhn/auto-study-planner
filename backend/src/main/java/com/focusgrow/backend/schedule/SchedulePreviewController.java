package com.focusgrow.backend.schedule;

import com.focusgrow.backend.goal.Goal;
import com.focusgrow.backend.goal.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.time.LocalDate;
import java.time.Clock;
import java.util.HashSet;
import java.util.Set;

/** 기존 목표 데이터로 알고리즘을 실행하는 미리보기 API. 일정 DB에는 저장하지 않는다. */
@RestController
@RequestMapping("/api/schedules")
public class SchedulePreviewController {

    private final GoalService goalService;
    private final ScheduleRepository schedules;
    private final Clock clock;
    private final ScheduleCalculator calculator = new ScheduleCalculator();

    public SchedulePreviewController(GoalService goalService, ScheduleRepository schedules, Clock clock) {
        this.schedules = schedules;
        this.clock = clock;
        this.goalService = goalService;
    }

    public record PreviewRequest(Set<LocalDate> excludedDates, Integer maxDailyAmount) {}

    public record PreviewResponse(
            Long goalId,
            String title,
            String unit,
            LocalDate calculationStartDate,
            String mode,
            ScheduleCalculator.Plan plan
    ) {}

    @PostMapping("/preview/{goalId}")
    public PreviewResponse preview(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("goalId") Long goalId,
            @RequestBody(required = false) PreviewRequest request
    ) {
        if (userId <= 0) {
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다.");
        }
        // 기존 GoalService가 본인 목표인지 확인한다. 임시 X-User-Id 방식은 JWT가 아니다.
        Goal goal = goalService.getGoal(userId, goalId);
        if (!"IN_PROGRESS".equals(goal.getStatus())) {
            throw conflict("진행 중인 목표만 미리 계산할 수 있습니다.");
        }
        if (goal.getTotalAmount() == null || goal.getStartDate() == null || goal.getEndDate() == null) {
            throw new IllegalArgumentException("목표의 총분량과 기간이 필요합니다.");
        }
        if (goal.getStartDate().isAfter(goal.getEndDate())) {
            throw new IllegalArgumentException("목표의 시작일과 마감일을 확인해 주세요.");
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate start = goal.getStartDate().isBefore(today) ? today : goal.getStartDate();
        if (start.isAfter(goal.getEndDate())) {
            throw conflict("마감일이 지났습니다. 목표 기간을 수정해 주세요.");
        }
        Set<LocalDate> holidays = new HashSet<>();
        if (request != null && request.excludedDates() != null) holidays.addAll(request.excludedDates());
        // 완료된 날짜에는 새 일정을 중복 배정하지 않는다.
        schedules.findByGoal_GoalIdOrderByDateAsc(goalId).stream()
                .filter(Schedule::isCompleted).forEach(s -> holidays.add(s.getDate()));
        Integer limit = request == null ? null : request.maxDailyAmount();

        try {
            // 실제 저장된 완료 분량을 제외한 잔여 분량만 미리 계산한다.
            ScheduleCalculator.Plan plan = calculator.calculate(
                    goal.getTotalAmount(), goalService.completedAmount(goalId), start, goal.getEndDate(), holidays, limit
            );
            return new PreviewResponse(goalId, goal.getTitle(), goal.getUnit(), start,
                    "REMAINING_PLAN_PREVIEW_NOT_SAVED", plan);
        } catch (IllegalStateException e) {
            throw conflict(e.getMessage());
        }
    }

    // 기존 GlobalExceptionHandler가 Exception을 500으로 처리하므로 이 컨트롤러에서 409를 직접 처리한다.
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public com.focusgrow.backend.common.ErrorResponse handleConflict(ResponseStatusException e) {
        return new com.focusgrow.backend.common.ErrorResponse(409, "CONFLICT", e.getReason());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public com.focusgrow.backend.common.ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
        return new com.focusgrow.backend.common.ErrorResponse(400, "BAD_REQUEST", "X-User-Id 헤더가 필요합니다.");
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
