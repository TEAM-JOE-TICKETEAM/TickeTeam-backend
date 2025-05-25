package com.tickeTeam.domain.sectionPrice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SectionPriceResponse {

    private String sectionName;
    private int price;
    private int totalPrice;

    public static SectionPriceResponse of(String sectionName, int price, int seatCount){
        return SectionPriceResponse.builder()
                .sectionName(sectionName)
                .price(price)
                .totalPrice(price*seatCount)
                .build();
    }
}
