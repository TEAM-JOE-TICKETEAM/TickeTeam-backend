package com.tickeTeam.infrastructure.security.authentication;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
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
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ResultResponse> login(@RequestBody LoginRequest loginRequest,
                                                HttpServletResponse response) {
        return ResponseEntity.ok(loginService.login(loginRequest, response));
    }
}
