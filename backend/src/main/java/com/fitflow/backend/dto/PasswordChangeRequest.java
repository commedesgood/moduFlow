package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "비밀번호 변경 요청")
public class PasswordChangeRequest {

    @NotBlank(message = "currentPassword is required")
    @Schema(description = "현재 비밀번호", example = "oldPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    @Schema(description = "새 비밀번호. 최소 8자", example = "newPassword123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String newPassword;

    @NotBlank(message = "newPasswordConfirm is required")
    @Schema(description = "새 비밀번호 확인", example = "newPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPasswordConfirm;
}
