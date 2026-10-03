package com.focusgrow.backend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 임시: JWT 적용 전까지 X-User-Id 헤더로 사용자 구분 (없으면 1)
    // TODO: JWT 적용 시 토큰에서 userId를 꺼내도록 교체
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(new UserMeResponse(
                user.getUserId(), user.getEmail(), user.getNickname(), user.getCreatedAt()
        ));
    }

    public record UserMeResponse(
            Long userId,
            String email,
            String nickname,
            LocalDateTime createdAt
    ) {}
}