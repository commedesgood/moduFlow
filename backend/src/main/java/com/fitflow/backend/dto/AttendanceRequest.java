package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "출석 요청. userName은 하위 호환용 필드이며 서버에서는 JWT 사용자 정보를 사용합니다.")
public class AttendanceRequest {
    @Schema(description = "Deprecated: 서버에서 무시합니다. 출석 사용자는 JWT로 결정됩니다.", example = "ignored-user", deprecated = true)
    private String userName;

    @NotBlank(message = "gymName은 필수입니다.")
    @Schema(description = "출석할 헬스장 이름", example = "Fit Gym", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gymName;
}
