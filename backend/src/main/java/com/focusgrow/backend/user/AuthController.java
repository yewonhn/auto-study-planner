package com.focusgrow.backend.user;

import com.focusgrow.backend.user.dto.LoginRequest;
import com.focusgrow.backend.user.dto.LoginResponse;
import com.focusgrow.backend.user.dto.SignupRequest;
import com.focusgrow.backend.user.dto.SignupResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        User user = userService.signup(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        // TODO: JWT 구현 시 실제 토큰으로 교체 (현재 임시 문자열)
        return ResponseEntity.ok(new LoginResponse("TEMP_ACCESS_TOKEN", user));
    }
}