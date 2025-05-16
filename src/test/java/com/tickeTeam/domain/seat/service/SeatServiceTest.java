package com.tickeTeam.domain.seat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private Game mockGame;

    @Mock
    private GameSeatsResponse mockGameSeatsResponse;

    @InjectMocks
    private SeatService seatService;

    private Long testGameId;
    private List<Seat> mockSeats;

    @BeforeEach
    void setup(){
        testGameId = 1L;
        mockGame = Game.builder()
                .id(1L)
                .stadium(Stadium.of("잠실 야구장"))
                .build();

    }

    @Test
    @DisplayName("경기 좌석 조회 성공 - 해당 경기에 해당하는 좌석 목록을 반환합니다.")
    void 경기_좌석_조회_성공(){
        try(MockedStatic<GameSeatsResponse> mockedResponse = mockStatic(GameSeatsResponse.class)) {
            // 준비
            when(gameRepository.findById(testGameId)).thenReturn(Optional.of(mockGame));

            mockSeats = List.of(mock(Seat.class), mock(Seat.class));
            when(seatRepository.findAllByGame(mockGame)).thenReturn(mockSeats);

            mockedResponse.when(() -> GameSeatsResponse.of(
                    eq(mockSeats), eq(testGameId), eq("잠실 야구장")
            )).thenReturn(mockGameSeatsResponse);

            // 실행
            ResultResponse resultResponse = seatService.getGameSeats(testGameId);

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getMessage());
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getCode());
            assertThat(resultResponse.getData()).isEqualTo(mockGameSeatsResponse);

            verify(gameRepository).findById(testGameId);
            verify(seatRepository).findAllByGame(mockGame);
            mockedResponse.verify(() -> GameSeatsResponse.of(
                    mockSeats, testGameId, "잠실 야구장"
            ));
        }
    }

    @Test
    @DisplayName("경기 좌석 조회 성공 - 해당 경기에 해당하는 좌석이 없으면 빈 리스트를 반환합니다.")
    void 경기_좌석_조회_성공_좌석_없음(){
        try(MockedStatic<GameSeatsResponse> mockedResponse = mockStatic(GameSeatsResponse.class)) {
            // 준비
            when(gameRepository.findById(testGameId)).thenReturn(Optional.of(mockGame));

            mockSeats = Collections.emptyList();
            when(seatRepository.findAllByGame(mockGame)).thenReturn(mockSeats);

            mockedResponse.when(() -> GameSeatsResponse.of(
                    eq(mockSeats), eq(testGameId), eq("잠실 야구장")
            )).thenReturn(mockGameSeatsResponse);
            when(mockGameSeatsResponse.getSeats()).thenReturn(Collections.emptyList());

            // 실행
            ResultResponse resultResponse = seatService.getGameSeats(testGameId);

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getMessage());
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getCode());
            assertThat(resultResponse.getData()).isEqualTo(mockGameSeatsResponse);

            GameSeatsResponse gameSeatsResponse = (GameSeatsResponse) resultResponse.getData();
            assertThat(gameSeatsResponse.getSeats()).isEqualTo(Collections.emptyList());

            verify(gameRepository).findById(testGameId);
            verify(seatRepository).findAllByGame(mockGame);
            mockedResponse.verify(() -> GameSeatsResponse.of(
                    mockSeats, testGameId, "잠실 야구장"
            ));
        }
    }

    @Test
    @DisplayName("경기 좌석 조회 실패 - ID에 해당하는 경기를 찾지 못함")
    void 경기_좌석_조회_실패_경기_못찾음(){
        // 준비
        when(gameRepository.findById(testGameId)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> seatService.getGameSeats(testGameId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.MATCH_NOT_FOUND.getMessage());

        verify(gameRepository).findById(testGameId);
        verifyNoInteractions(seatRepository);
    }

}