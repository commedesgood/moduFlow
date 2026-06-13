package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Authenticated user summary")
public class UserInfoResponse {

    @Schema(description = "Frontend display user id", example = "u_1")
    private final String id;

    @Schema(description = "Email", example = "user@example.com")
    private final String email;

    @Schema(description = "Display name", example = "User")
    private final String name;

    @Schema(description = "User role", example = "ADMIN")
    private final String role;

    public UserInfoResponse(String id, String email, String name) {
        this(id, email, name, "USER");
    }

    public UserInfoResponse(String id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
