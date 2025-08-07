package com.tickeTeam.domain.payment.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.payment.dto.request.PaymentRequest;
import com.tickeTeam.domain.payment.entity.Payment;
import com.tickeTeam.domain.payment.entity.PaymentMethod;
import com.tickeTeam.domain.payment.entity.PaymentStatus;
import com.tickeTeam.domain.payment.repository.PaymentRepository;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;
    private final StringRedisTemplate stringRedisTemplate; // <-- 추가

    public Long pay(PaymentRequest request) {
        Member member = getMemberByAuthentication();
        List<Seat> seats = seatRepository.findByIdIn(request.seatIds());
        checkIsHold(seats, member); // 각 좌석들이 해당 사용자에게 선점된 좌석이 맞는지 확인

        log.info("결제 요청 완료 - 사용자: {}, 금액: {}", member.getEmail(), request.totalPrice());

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .totalPrice(request.totalPrice())
                .paymentMethod(PaymentMethod.valueOf(request.paymentMethod()))
                .paymentStatus(PaymentStatus.valueOf(request.paymentStatus()))
                .paidMemberEmail(request.email())
                .paidAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment).getId();
    }

    private void checkIsHold(List<Seat> seats, Member member) {
        for (Seat seat : seats) {
            String holdKey = "seat:" + seat.getId() + ":heldBy";
            String holderId = stringRedisTemplate.opsForValue().get(holdKey); // <-- 수정
            System.out.println("horderId: "+ holderId);
            if (holderId == null) {
                throw new BusinessException(ErrorCode.SEAT_NOT_HELD);
            }
            if (!holderId.equals(member.getEmail())) {
                throw new BusinessException(ErrorCode.SEAT_HELD_BY_OTHER);
            }
        }
    }

    private Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }

        String memberEmail = authentication.getName();
        return memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

}
