package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profile name update request")
public class ProfileNameUpdateRequest {

    @JsonAlias({"displayName", "userName", "nickname"})
    @Schema(description = "New display name", example = "새 이름")
    private String name;

    public String getName() {
        return name;
    }
}
