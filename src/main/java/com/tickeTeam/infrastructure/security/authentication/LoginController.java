package com.tickeTeam.infrastructure.security.authentication;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "LoginController", description = "로그인 API")
public class LoginController {

    private final LoginService loginService;

    @Operation(
            summary = "로그인 api",
            description = "로그인 요청을 처리합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ResultResponse> login(@RequestBody LoginRequest loginRequest,
                                                HttpServletResponse response) {
        return ResponseEntity.ok(loginService.login(loginRequest, response));
    }
}
