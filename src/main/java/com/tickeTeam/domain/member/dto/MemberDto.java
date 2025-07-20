package com.tickeTeam.domain.member.dto;


import com.tickeTeam.domain.member.entity.MemberRole;
import java.io.Serializable;

public record MemberDto(
        Long id,
        String name,
        String email,
        String password,
        String favoriteTeamName,
        MemberRole role
) implements Serializable {}
