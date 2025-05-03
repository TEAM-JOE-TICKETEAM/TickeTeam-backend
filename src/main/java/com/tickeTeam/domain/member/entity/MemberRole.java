package com.tickeTeam.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN"),
    UNKNOWN("UNKNOWN");

    private final String roleName;

    public static MemberRole fromString(String roleName){
        for(MemberRole role : values()){
            if (role.roleName.equalsIgnoreCase(roleName)){
                return role;
            }
        }
        return UNKNOWN;
    }
}
