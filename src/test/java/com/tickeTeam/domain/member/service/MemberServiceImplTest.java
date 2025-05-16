package com.tickeTeam.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.domain.member.dto.response.MyPageResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class) // JUnit5에서 Mockito를 사용하기 위한 확장
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private Member mockExistingMember;

    @InjectMocks
    private MemberServiceImpl memberService;

    private static final String ACCESS_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private Member testMember;
    private MemberSignUpRequest testSignUpRequest;
    private MemberUpdateRequest testUpdateRequest;
    private Team testTeam;
    private Team updateTeam;
    private String testToken;
    private String testEmail;
    private String hashedPassword;
    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testToken = "valid.jwt.token";
        testTeam = Team.of("두산 베어스");
        updateTeam = Team.of("LG 트윈스");
        hashedPassword = "hashedPassword";
        testMember = Member.builder()
                .id(1L)
                .name("tester")
                .email("test@example.com")
                .favoriteTeam(testTeam)
                .password("test123")
                .role(MemberRole.USER)
                .build();

        testSignUpRequest = MemberSignUpRequest.builder()
                .name("tester")
                .email("test@example.com")
                .password("test123")
                .favoriteTeam("두산 베어스")
                .role(MemberRole.USER)
                .build();

        testUpdateRequest = MemberUpdateRequest.builder()
                .name("updateTester")
                .favoriteTeam("LG 트윈스")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 - 회원 ID 반환")
    void 회원가입_성공(){
        // 준비
        when(memberRepository.existsByEmail(testSignUpRequest.getEmail())).thenReturn(false);
        when(teamRepository.findByTeamName(testSignUpRequest.getFavoriteTeam())).thenReturn(Optional.of(testTeam));
        when(bCryptPasswordEncoder.encode(testSignUpRequest.getPassword())).thenReturn(hashedPassword);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // 실행
        Long newMemberId = (Long) memberService.signUp(testSignUpRequest).getData();

        // 검증
        assertThat(newMemberId).isEqualTo(1L);

        verify(memberRepository).existsByEmail(testSignUpRequest.getEmail());
        verify(teamRepository).findByTeamName(testSignUpRequest.getFavoriteTeam());
        verify(bCryptPasswordEncoder).encode(testSignUpRequest.getPassword());
        ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberArgumentCaptor.capture());
        Member memberToSave = memberArgumentCaptor.getValue();

        assertThat(memberToSave.getEmail()).isEqualTo(testSignUpRequest.getEmail());
        assertThat(memberToSave.getPassword()).isEqualTo(hashedPassword); // 인코딩된 비밀번호로 저장되는지
        assertThat(memberToSave.getName()).isEqualTo(testSignUpRequest.getName());
        assertThat(memberToSave.getFavoriteTeam()).isEqualTo(testTeam);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void 회원가입_실패_이메일_중복(){
        // 준비
        when(memberRepository.existsByEmail(testSignUpRequest.getEmail())).thenReturn(true);

        // 실행
        ResultResponse resultResponse = memberService.signUp(testSignUpRequest);

        // 검증
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.CHECK_EMAIL.getMessage());
        assertThat(resultResponse.getCode()).isEqualTo(ResultCode.CHECK_EMAIL.getCode());

        verify(memberRepository).existsByEmail(testSignUpRequest.getEmail());
    }

    @Test
    @DisplayName("회원가입 실패 - 팀을 찾을 수 없음")
    void 회원가입_실패_팀_없음(){
        // 준비
        when(memberRepository.existsByEmail(testSignUpRequest.getEmail())).thenReturn(false);
        when(teamRepository.findByTeamName(testSignUpRequest.getFavoriteTeam())).thenReturn(Optional.empty());

        // 실행
        assertThatThrownBy(() -> memberService.signUp(testSignUpRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());

        // 검증
        verify(memberRepository).existsByEmail(testSignUpRequest.getEmail());
        verify(teamRepository).findByTeamName(testSignUpRequest.getFavoriteTeam());
    }

    @Test
    @DisplayName("마이 페이지 조회 성공 - 회원 정보 DTO 반환")
    void 마이_페이지_성공(){
        // 준비
        when(request.getHeader(ACCESS_HEADER)).thenReturn(BEARER_PREFIX + testToken);
        when(jwtUtil.getEmail(testToken)).thenReturn(testEmail);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(testMember));

        // 실행
        MyPageResponse myPageResponse = (MyPageResponse) memberService.myPage(request).getData();

        // 검증
        assertThat(myPageResponse).isNotNull();
        assertThat(myPageResponse.getName()).isEqualTo(testMember.getName());
        assertThat(myPageResponse.getEmail()).isEqualTo(testMember.getEmail());
        assertThat(myPageResponse.getFavoriteTeam()).isEqualTo(testMember.getFavoriteTeam().getTeamName());

        // Mock 객체들의 메서드가 올바르게 호출되었는지 검증
        verify(request).getHeader(ACCESS_HEADER);
        verify(jwtUtil).getEmail(testToken);
        verify(memberRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("마이 페이지 조회 실패 - 토큰 없음")
    void 마이_페이지_실패_토큰_없음(){
        // 준비
        when(request.getHeader(ACCESS_HEADER)).thenReturn(null);

        // 실행 & 검증
        assertThatThrownBy(() -> memberService.myPage(request))
                .isInstanceOf(BusinessException.class)
                        .hasMessage(ErrorCode.TOKEN_ACCESS_NOT_EXIST.getMessage());

        verify(request).getHeader(ACCESS_HEADER);
        verifyNoInteractions(jwtUtil); // jwtUtil 호출 안되어야 함
        verifyNoInteractions(memberRepository); // memberRepository 호출 안되어야 함
    }

    @Test
    @DisplayName("마이 페이지 조회 실패 - 추출한 이메일로 사용자 찾을 수 없음")
    void 마이_페이지_사용자_조회_실패(){
        // 준비
        when(request.getHeader(ACCESS_HEADER)).thenReturn(BEARER_PREFIX + testToken);
        when(jwtUtil.getEmail(testToken)).thenReturn(testEmail);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> memberService.myPage(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());


        verify(request).getHeader(ACCESS_HEADER);
        verify(jwtUtil).getEmail(testToken);
        verify(memberRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void 사용자_정보_수정_성공(){
        // 준비
        when(request.getHeader(ACCESS_HEADER)).thenReturn(BEARER_PREFIX + testToken);
        when(jwtUtil.getEmail(testToken)).thenReturn(testEmail);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockExistingMember));
        when(teamRepository.findByTeamName(testUpdateRequest.getFavoriteTeam())).thenReturn(Optional.of(updateTeam));

        // 실행
        ResultResponse resultResponse = memberService.updateMember(testUpdateRequest, request);

        // 검증
        assertThat(resultResponse).isNotNull();
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.MEMBER_UPDATE_SUCCESS.getMessage());

        verify(request).getHeader(ACCESS_HEADER);
        verify(jwtUtil).getEmail(testToken);
        verify(memberRepository).findByEmail(testEmail);
        verify(teamRepository).findByTeamName(testUpdateRequest.getFavoriteTeam());
        verify(mockExistingMember).update(testUpdateRequest, updateTeam);
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 팀 찾을 수 없음")
    void 사용자_수정_실패_팀_못찾음() {
        when(request.getHeader(ACCESS_HEADER)).thenReturn(BEARER_PREFIX + testToken);
        when(jwtUtil.getEmail(testToken)).thenReturn(testEmail);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockExistingMember));
        when(teamRepository.findByTeamName(testUpdateRequest.getFavoriteTeam())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMember(testUpdateRequest, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());

        verify(request).getHeader(ACCESS_HEADER);
        verify(jwtUtil).getEmail(testToken);
        verify(memberRepository).findByEmail(testEmail);
        verify(teamRepository).findByTeamName(testUpdateRequest.getFavoriteTeam());
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 추출한 이메일로 사용자 찾을 수 없음")
    void 사용자_정보_수정_실패_사용자_못찾음(){
        // 준비
        when(request.getHeader(ACCESS_HEADER)).thenReturn(BEARER_PREFIX + testToken);
        when(jwtUtil.getEmail(testToken)).thenReturn(testEmail);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> memberService.updateMember(testUpdateRequest, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(request).getHeader(ACCESS_HEADER);
        verify(jwtUtil).getEmail(testToken);
        verify(memberRepository).findByEmail(testEmail);
        verifyNoInteractions(teamRepository);
    }
}