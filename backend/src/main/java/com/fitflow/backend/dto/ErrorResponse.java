package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공통 에러 응답")
public class ErrorResponse {
    @Schema(description = "에러 코드", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "사용자에게 표시 가능한 에러 메시지", example = "name은 필수입니다.")
    private String message;
}
