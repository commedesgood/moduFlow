package com.fitflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
public class LoginRequest {
    @JsonAlias({"id", "username", "userId"})
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @JsonAlias({"pw", "pwd"})
    @NotBlank(message = "password is required")
    private String password;
}
