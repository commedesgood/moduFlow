package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 요약 정보")
public class UserInfoResponse {
    @Schema(description = "프론트 표시용 사용자 id", example = "u_1")
    private String id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "User")
    private String name;
}
