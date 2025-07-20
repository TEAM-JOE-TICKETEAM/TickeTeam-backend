package com.tickeTeam.domain.member.service;

import com.tickeTeam.common.annotation.Trace;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.domain.member.dto.request.MemberVerificationRequest;
import com.tickeTeam.domain.member.dto.response.MyPageResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.member.dto.MemberDto;
import com.tickeTeam.domain.ticket.dto.response.ReservationListResponse;
import com.tickeTeam.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService{

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TicketService ticketService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 회원 가입
    @Transactional
    public ResultResponse signUp(MemberSignUpRequest memberSignUpRequest) {

        // 이메일 중복 검사
        String email = memberSignUpRequest.getEmail();
        if (memberRepository.existsByEmail(email)) {
            return ResultResponse.of(ResultCode.CHECK_EMAIL, true);
        }

        // 응원 팀 설정
        Team favoriteTeam = teamRepository.findByTeamName(memberSignUpRequest.getFavoriteTeam()).orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)
        );

        // 비밀번호 인코딩
        String hashedPassword = bCryptPasswordEncoder.encode(memberSignUpRequest.getPassword());

        // 실제 객체 저장
        Member newMember = memberRepository.save(Member.of(memberSignUpRequest, hashedPassword, favoriteTeam));

        return ResultResponse.of(ResultCode.MEMBER_CREATE_SUCCESS, newMember.getId());
    }

    // 회원 정보 조회
    public ResultResponse myPage() {
        Member findMember = getMemberByAuthentication();

        ReservationListResponse reservationInfoList = ticketService.getReservationInfoList(findMember);

        return ResultResponse.of(ResultCode.MY_PAGE, MyPageResponse.from(findMember, reservationInfoList));
    }

    // 회원 정보 수정
    @Transactional
    public ResultResponse updateMember(MemberUpdateRequest memberUpdateRequest) {
        Member findMember = getMemberByAuthentication();
        Team findTeam = teamRepository.findByTeamName(memberUpdateRequest.getFavoriteTeam()).orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)
        );

        findMember.update(memberUpdateRequest, findTeam);

        return ResultResponse.of(ResultCode.MEMBER_UPDATE_SUCCESS);
    }

    public ResultResponse memberVerification(MemberVerificationRequest memberVerificationRequest) {
        Member findMember = getMemberByAuthentication();

        if (!memberVerificationRequest.getEmail().equals(findMember.getEmail()) || !memberVerificationRequest.getName().equals(findMember.getName())){
            throw new BusinessException(ErrorCode.MEMBER_VERIFICATION_FAIL);
        }

        return ResultResponse.of(ResultCode.MEMBER_VERIFICATION_SUCCESS);
    }

    //@Cacheable(value = "memberByEmail", key = "#email", cacheResolver = "cacheResolver")
    @Cacheable(value = "memberByEmail", key = "#email")
    @Transactional(readOnly = true)
    public MemberDto getMemberByEmail(String email){
        log.info(">>> getMemberByEmail() readOnly = {}", TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        Member member = memberRepository.findByEmailWithTeam(email).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        return new MemberDto(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getFavoriteTeam().getTeamName(),
                member.getRole()
        );
    }

    public Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }
        String memberEmail = authentication.getName();
        return memberRepository.findByEmailWithTeam(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }
}
