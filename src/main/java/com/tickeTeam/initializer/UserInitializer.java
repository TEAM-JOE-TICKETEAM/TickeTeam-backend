package com.tickeTeam.initializer;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {

    private static final int USER_NUM = 10;

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Team favoriteTeam = teamRepository.findByTeamName("두산 베어스").orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)
        );

        List<Member> createdMembers = new ArrayList<>();
        for (int i = 0; i < USER_NUM; i++) {
            Member createdMember = Member.builder()
                    .name("tester" + i)
                    .email("test" + i + "@example.com")
                    .password(bCryptPasswordEncoder.encode("test"))
                    .favoriteTeam(favoriteTeam)
                    .role(MemberRole.USER)
                    .build();
            createdMembers.add(createdMember);
        }

        memberRepository.saveAll(createdMembers);
    }
}
