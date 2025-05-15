package com.tickeTeam.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberUpdateRequest {

    private final String name;
    private final String favoriteTeam;
}
