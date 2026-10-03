package com.focusgrow.backend.user.dto;

import com.focusgrow.backend.user.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final UserInfo user;

    public LoginResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.user = new UserInfo(user);
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String nickname;

        public UserInfo(User user) {
            this.userId = user.getUserId();
            this.nickname = user.getNickname();
        }
    }
}