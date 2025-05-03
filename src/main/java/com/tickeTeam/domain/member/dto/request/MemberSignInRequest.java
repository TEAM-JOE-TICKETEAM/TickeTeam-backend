package com.tickeTeam.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberSignInRequest {

    private final String email;
    private final String password;
}
