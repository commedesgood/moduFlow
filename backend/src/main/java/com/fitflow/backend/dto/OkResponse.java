package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "성공 여부 응답")
public class OkResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean ok;
}
