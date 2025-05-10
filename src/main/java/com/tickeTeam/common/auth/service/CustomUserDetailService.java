package com.tickeTeam.common.auth.service;

import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.common.auth.dto.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 사용자 인증을 위한 사용자 정보 로드 클래스
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       Member foundMember = memberRepository.findByEmail(email).orElseThrow(
               () -> new UsernameNotFoundException("NOT EXIST MEMBER")
       );

        return new UserDetailsDto(foundMember);
    }
}
