package com.tickeTeam.domain.sectionPrice.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.sectionPrice.dto.request.SectionPriceRequest;
import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SectionPriceServiceTest {

    @Mock
    SectionPriceRepository sectionPriceRepository;

    @InjectMocks
    SectionPriceService sectionPriceService;

    private SectionPrice sectionPrice;
    private SectionPriceRequest sectionPriceRequest = new SectionPriceRequest("1루 내야석", 1, LocalDate.of(2025,5,21));

    @Test
    @DisplayName("선택한 좌석(들) 가격 정보 확인 성공")
    void 가격_정보_확인_성공(){
        sectionPrice = mock(SectionPrice.class);

        // 준비
        when(sectionPriceRepository.findBySeatSection(sectionPriceRequest.getSectionName())).thenReturn(sectionPrice);

        // 실행
        ResultResponse resultResponse = sectionPriceService.getSectionPrice(sectionPriceRequest);

        // 검증
        assertThat(resultResponse).isNotNull();
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.SECTION_PRICE_CHECK_SUCCESS.getMessage());

        verify(sectionPriceRepository).findBySeatSection(sectionPriceRequest.getSectionName());
    }

}