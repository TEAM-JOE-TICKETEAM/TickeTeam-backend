package com.tickeTeam.infrastructure.security.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginRequest {

    private final String email;
    private final String password;
}
