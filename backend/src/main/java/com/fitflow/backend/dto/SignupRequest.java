package com.fitflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
public class SignupRequest {
    @JsonAlias({"id", "username", "userId"})
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @JsonAlias({"pw", "pwd"})
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @JsonAlias({"confirmPw", "confirmPwd", "passwordConfirm", "passwordConfirmation"})
    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;
}
