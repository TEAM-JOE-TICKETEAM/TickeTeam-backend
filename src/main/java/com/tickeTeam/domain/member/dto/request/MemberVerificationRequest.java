package com.tickeTeam.domain.member.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberVerificationRequest {

    private String email;
    private String name;

    public static MemberVerificationRequest of(String email, String name){
        return new MemberVerificationRequest(email, name);
    }
}
