package com.tickeTeam.domain.seat.service;

import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatTransactionService {

    private final SeatRepository seatRepository;

    @Transactional // 이 메소드는 외부(SeatSelectService)에서 호출되므로 트랜잭션이 정상적으로 적용됨
    public void holdSeatsInNewTransaction(List<Long> seatIds) {
        // ID 목록으로 좌석들을 조회합니다.
        List<Seat> seatsToHold = seatRepository.findAllById(seatIds);

        // 상태 변경
        seatsToHold.forEach(Seat::seatHold);
    }
}
