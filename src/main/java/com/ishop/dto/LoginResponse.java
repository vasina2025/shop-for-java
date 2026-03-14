package com.ishop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "消息")
    private String message;

    public static LoginResponse success(Long userId, String username, String nickname, String token) {
        return new LoginResponse(true, userId, username, nickname, token, "登录成功");
    }

    public static LoginResponse fail(String message) {
        return new LoginResponse(false, null, null, null, null, message);
    }
}
