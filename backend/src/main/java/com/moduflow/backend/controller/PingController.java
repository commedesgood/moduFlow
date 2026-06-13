package com.moduflow.backend.controller;

import com.moduflow.backend.dto.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "상태 확인", description = "서버 상태 확인 API")
public class PingController {

    @GetMapping("/ping")
    @Operation(summary = "서버 상태 확인", description = "서버가 정상 동작 중인지 확인합니다.")
    public OkResponse ping() {
        return new OkResponse(true);
    }
}
