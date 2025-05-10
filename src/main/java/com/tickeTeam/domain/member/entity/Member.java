package com.tickeTeam.domain.member.entity;

import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE member_id = ?")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // 이름

    @Column(name = "email", unique = true, nullable = false)
    private String email; // 사용자 이메일

    @Column(name = "password", nullable = false)
    private String password; // 비밀번호

    @Column
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_team", nullable = false)
    private Team favoriteTeam; // 응원팀

    public static Member of(MemberSignUpRequest dto, String hashedPassword ,Team favoriteTeam){
        return Member.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(hashedPassword)
                .favoriteTeam(favoriteTeam)
                .role(dto.getRole())
                .build();
    }
}
