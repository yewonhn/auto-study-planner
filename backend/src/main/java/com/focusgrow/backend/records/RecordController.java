package com.focusgrow.backend.records;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    // TODO: RECORD 테이블 구현 후 실제 집계로 교체 (현재 임시 응답)
    @GetMapping
    public ResponseEntity<RecordSummaryResponse> getRecords(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        DailyRecord daily = new DailyRecord("2026-10-01", 4, 5, 80);
        return ResponseEntity.ok(new RecordSummaryResponse(3, 1, 1250, 82, List.of(daily)));
    }

    // TODO: 실제 월별 집계로 교체 (현재 임시 응답)
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyRecordResponse> getMonthly(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        String prefix = String.format("%04d-%02d", year, month);
        List<MonthlyItem> items = List.of(
                new MonthlyItem(prefix + "-01", 80),
                new MonthlyItem(prefix + "-02", 100)
        );
        return ResponseEntity.ok(new MonthlyRecordResponse(year, month, items));
    }

    public record DailyRecord(String date, Integer completedCount, Integer totalCount, Integer studyTime) {}

    public record RecordSummaryResponse(
            Integer totalGoals, Integer completedGoals, Integer totalStudyTime,
            Integer completionRate, List<DailyRecord> records
    ) {}

    public record MonthlyItem(String date, Integer completionRate) {}

    public record MonthlyRecordResponse(Integer year, Integer month, List<MonthlyItem> records) {}
}