package com.fitflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청")
public class AuthSignupRequest {
    @JsonAlias({"id", "username", "userId"})
    @NotBlank(message = "email은 필수입니다.")
    @Email(message = "email 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @JsonAlias({"pw", "pwd"})
    @NotBlank(message = "password는 필수입니다.")
    @Size(min = 8, message = "password는 최소 8자 이상이어야 합니다.")
    @Schema(description = "비밀번호. 최소 8자", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;
}
