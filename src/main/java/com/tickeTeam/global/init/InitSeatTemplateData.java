package com.tickeTeam.global.init;

import com.tickeTeam.domain.seat.entity.SeatInfo;
import com.tickeTeam.domain.seat.entity.SeatTemplate;
import com.tickeTeam.domain.seat.entity.SeatType;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 구장별 규격화된 좌석 정보(잠실 야구장 기준)
 * 23000석 전부 삽입하기에 양이 너무 많기에 범위를 좁혀 삽입
 * 지정석 1블럭 좌석 수 - 14 * 17(238)
 * 비지정석 1블럭 좌석 수 - 15 * 15(225)
 * 지정석 2블럭(476석) / 비지정석 2블럭(450석)
 * 총 926석
 */

@Order(1)
@Component
@RequiredArgsConstructor
public class InitSeatTemplateData implements ApplicationRunner {

    // 좌석 관련 상수
    private static final String ASSIGNED_SECTION = "1루 레드석";
    private static final String NON_ASSIGNED_SECTION = "3루 외야 일반석";
    private static final String ASSIGNED_BLOCK1 = "329";
    private static final String ASSIGNED_BLOCK2 = "330";
    private static final String NON_ASSIGNED_BLOCK1 = "116";
    private static final String NON_ASSIGNED_BLOCK2 = "117";
    private static final int ASSIGNED_ROWS = 34;
    private static final int ASSIGNED_COLS = 14;
    private static final int NON_ASSIGNED_ROWS = 30;
    private static final int NON_ASSIGNED_COLS = 15;
    private final SeatTemplateRepository seatTemplateRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 좌석이 없으면 삽입
        if (seatTemplateRepository.count() == 0) {
            List<SeatTemplate> seatTemplates = new ArrayList<>();

            // 지정석 생성
            seatTemplates.addAll(generateAssignedSeats());

            // 비지정석 생성
            seatTemplates.addAll(generateNonAssignedSeats());

            // 데이터 저장
            seatTemplateRepository.saveAll(seatTemplates);
        }
    }

    // 지정석 좌석 생성 메서드
    private List<SeatTemplate> generateAssignedSeats() {
        List<SeatTemplate> seats = new ArrayList<>();

        for (int row = 0; row < ASSIGNED_ROWS; row++) {
            // 블럭 구분
            String block = row < 17 ? ASSIGNED_BLOCK1 : ASSIGNED_BLOCK2;
            for (int col = 0; col < ASSIGNED_COLS; col++) {
                seats.add(buildSeatTemplate(SeatType.ASSIGNED, ASSIGNED_SECTION, block, row, col));
            }
        }
        return seats;
    }

    // 비지정석 좌석 생성 메서드
    private List<SeatTemplate> generateNonAssignedSeats() {
        List<SeatTemplate> seats = new ArrayList<>();

        for (int row = 0; row < NON_ASSIGNED_ROWS; row++) {
            // 블럭 구분
            String block = row < 15 ? NON_ASSIGNED_BLOCK1 : NON_ASSIGNED_BLOCK2;
            for (int col = 0; col < NON_ASSIGNED_COLS; col++) {
                // 비지정석은 row와 seatNum이 의미 없으므로 null로 설정
                seats.add(buildSeatTemplate(SeatType.NON_ASSIGNED, NON_ASSIGNED_SECTION, block, null, null));
            }
        }
        return seats;
    }

    // SeatTemplate 객체 빌드하는 메서드
    private SeatTemplate buildSeatTemplate(SeatType seatType, String section, String block, Integer row, Integer num) {
        return SeatTemplate.builder()
                .seatInfo(SeatInfo.builder()
                        .seatType(seatType)
                        .seatSection(section)
                        .seatBlock(block)
                        .seatRow(row != null ? String.valueOf(row) : null)  // null 처리
                        .seatNum(num)  // num은 null일 수 있도록 Integer로 처리
                        .build())
                .build();
    }
}
