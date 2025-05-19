package com.tickeTeam.domain.sectionPrice.service;

import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.sectionPrice.dto.request.SectionPriceRequest;
import com.tickeTeam.domain.sectionPrice.dto.response.SectionPriceResponse;
import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SectionPriceService {

    private final SectionPriceRepository sectionPriceRepository;

    public ResultResponse getSectionPrice(SectionPriceRequest request) {
        SectionPrice sectionPrice = sectionPriceRepository.findBySeatSection(request.getSectionName());

        int price = sectionPrice.getSectionPrice();
        int dayOfWeekValue = request.getMatchDay().getDayOfWeek().getValue();
        if (dayOfWeekValue == 6 || dayOfWeekValue == 7){
            price = (int)Math.floor(sectionPrice.getSectionPrice() * 1.1);
        }

        return ResultResponse.of(ResultCode.SECTION_PRICE_CHECK_SUCCESS,
                SectionPriceResponse.of(request.getSectionName(), price, request.getSeatCount()));
    }
}
