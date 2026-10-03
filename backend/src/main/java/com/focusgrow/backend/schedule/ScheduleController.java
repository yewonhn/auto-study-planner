package com.focusgrow.backend.schedule;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    // TODO: SCHEDULE 테이블 구현 후 실제 조회로 교체 (현재 임시 응답)
    @GetMapping("/today")
    public ResponseEntity<TodayScheduleResponse> today(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        ScheduleItem item = new ScheduleItem(1L, 1L, "책 10페이지 읽기", 10, "19:00", "19:30", false);
        return ResponseEntity.ok(new TodayScheduleResponse(LocalDate.now(), List.of(item)));
    }

    // TODO: 실제 완료 처리(RECORD 생성, 캐릭터 경험치 반영) 구현 (현재 임시 응답)
    @PatchMapping("/{scheduleId}/complete")
    public ResponseEntity<CompleteResponse> complete(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(new CompleteResponse(scheduleId, true, LocalDateTime.now()));
    }

    public record ScheduleItem(
            Long scheduleId, Long goalId, String title, Integer amount,
            String startTime, String endTime, Boolean completed
    ) {}

    public record TodayScheduleResponse(LocalDate date, List<ScheduleItem> schedules) {}

    public record CompleteResponse(Long scheduleId, Boolean completed, LocalDateTime completedAt) {}
}