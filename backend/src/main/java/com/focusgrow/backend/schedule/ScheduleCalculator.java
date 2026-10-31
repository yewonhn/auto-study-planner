package com.focusgrow.backend.schedule;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 목표의 남은 분량을 날짜별로 나누는 계산 전용 클래스. DB는 변경하지 않는다. */
public class ScheduleCalculator {

    public record DailyPlan(LocalDate date, int amount) {}

    public record Plan(
            int remainingAmount,
            int availableDayCount,
            List<DailyPlan> schedules
    ) {}

    // 가장 단순한 사용: 시작일~마감일 전체를 사용한다.
    public Plan calculate(int totalAmount, LocalDate startDate, LocalDate endDate) {
        return calculate(totalAmount, 0, startDate, endDate, Set.of(), null);
    }

    /**
     * completedAmount: 이미 끝낸 실제 분량. 새로 배정하지 않는다.
     * excludedDates: 휴무일. null 또는 빈 집합이면 휴무일이 없다.
     * maxDailyAmount: 목표 하나의 하루 최대 분량. null이면 제한하지 않는다.
     * startDate: 이번 계산을 시작할 날짜. 재계산이라면 기준일을 전달한다.
     */
    public Plan calculate(
            int totalAmount,
            int completedAmount,
            LocalDate startDate,
            LocalDate endDate,
            Set<LocalDate> excludedDates,
            Integer maxDailyAmount
    ) {
        // 1. 잘못된 입력을 먼저 걸러낸다.
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일과 마감일을 확인해 주세요.");
        }
        if (totalAmount <= 0 || completedAmount < 0 || completedAmount > totalAmount) {
            throw new IllegalArgumentException("총분량은 양수, 완료 분량은 0~총분량이어야 합니다.");
        }
        if (maxDailyAmount != null && maxDailyAmount <= 0) {
            throw new IllegalArgumentException("하루 최대 분량은 양수여야 합니다.");
        }
        // 지나치게 큰 요청을 막는 MVP 입력 정책: 최대 3,650일. 필요하면 팀과 변경한다.
        if (ChronoUnit.DAYS.between(startDate, endDate) >= 3650) {
            throw new IllegalArgumentException("한 번에 계산할 수 있는 기간은 최대 3,650일입니다.");
        }

        int remainingAmount = totalAmount - completedAmount;
        if (remainingAmount == 0) {
            return new Plan(0, 0, List.of());
        }

        // 2. 시작일과 마감일을 포함하되 휴무일은 뺀다.
        Set<LocalDate> holidays = excludedDates == null ? Set.of() : excludedDates;
        List<LocalDate> studyDates = new ArrayList<>();
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < dayCount; i++) {
            LocalDate date = startDate.plusDays(i);
            if (!holidays.contains(date)) {
                studyDates.add(date);
            }
        }
        if (studyDates.isEmpty()) {
            throw new IllegalStateException("공부할 수 있는 날짜가 없습니다. 휴무일이나 기간을 바꿔 주세요.");
        }
        if (maxDailyAmount != null
                && remainingAmount > (long) studyDates.size() * maxDailyAmount) {
            throw new IllegalStateException("기간 안에 분량을 배정할 수 없습니다. 기간이나 하루 한도를 늘려 주세요.");
        }

        // 3. 예: 10쪽 / 3일 = 기본 3쪽, 나머지 1쪽.
        int baseAmount = remainingAmount / studyDates.size();
        int extraAmount = remainingAmount % studyDates.size();

        // 4. 앞 날짜부터 나머지를 1씩 더한다. 예: [4, 3, 3].
        List<DailyPlan> schedules = new ArrayList<>();
        for (int i = 0; i < studyDates.size(); i++) {
            int amount = baseAmount;
            if (i < extraAmount) {
                amount++;
            }
            // 분량보다 날짜가 많아도 0쪽짜리 일정은 만들지 않는다.
            if (amount > 0) {
                schedules.add(new DailyPlan(studyDates.get(i), amount));
            }
        }
        return new Plan(remainingAmount, studyDates.size(), List.copyOf(schedules));
    }
}
