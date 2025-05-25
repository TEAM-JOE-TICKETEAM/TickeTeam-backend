package com.tickeTeam.domain.member.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class MemberVerificationRequest {

    private String email;
    private String name;

    public static MemberVerificationRequest of(String email, String name){
        return new MemberVerificationRequest(email, name);
    }
}
