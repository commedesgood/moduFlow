package com.fitflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SettingsRequest {
    @NotNull(message = "autoAttendanceEnabled는 필수입니다.")
    private Boolean autoAttendanceEnabled;
}

