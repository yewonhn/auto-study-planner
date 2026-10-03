package com.focusgrow.backend.character;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    // TODO: CHARACTER 테이블 구현 후 실제 조회로 교체 (현재 임시 응답)
    @GetMapping
    public ResponseEntity<CharacterResponse> getCharacter(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        return ResponseEntity.ok(new CharacterResponse(1L, 3, 240, 300));
    }

    // TODO: 성장 기록 저장 구현 후 실제 조회로 교체 (현재 임시 응답)
    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        return ResponseEntity.ok(new HistoryResponse(List.of(
                new HistoryItem("2026-10-01", 10, "일정 완료")
        )));
    }

    public record CharacterResponse(Long characterId, Integer level, Integer experience, Integer nextLevelExperience) {}

    public record HistoryItem(String date, Integer experience, String reason) {}

    public record HistoryResponse(List<HistoryItem> history) {}
}