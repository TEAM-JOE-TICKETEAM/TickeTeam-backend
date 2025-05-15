package com.tickeTeam.domain.member.dto.request;

import com.tickeTeam.domain.member.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignUpRequest {

    private String name;

    private String email;

    private String password;

    private String favoriteTeam;

    private MemberRole role; // USER, ADMIN
}
