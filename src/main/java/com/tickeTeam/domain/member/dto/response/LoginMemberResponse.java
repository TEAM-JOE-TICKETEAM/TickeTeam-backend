package com.tickeTeam.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginMemberResponse {

    private final String email;

    private final String name;

    private final String favoriteTeam;
}
