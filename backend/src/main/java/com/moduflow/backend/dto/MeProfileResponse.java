package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "My profile response")
public record MeProfileResponse(
        Long id,
        String email,
        String name
) {
}
