package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "Signup request")
public class AuthSignupRequest {
    @JsonAlias({"id", "username", "userId"})
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Schema(description = "Email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @JsonAlias({"pw", "pwd"})
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    @Schema(description = "Password. Minimum 8 characters.", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;

    @JsonAlias({"nickname", "userName", "displayName"})
    @Size(max = 100, message = "name must be at most 100 characters")
    @Schema(description = "User display name or nickname", example = "Hong Gil-dong", maxLength = 100)
    private String name;
}
