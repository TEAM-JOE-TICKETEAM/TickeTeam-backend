package com.tickeTeam.infrastructure.security.authentication.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final String email;

    private final String name;

    private final String favoriteTeam;
}
