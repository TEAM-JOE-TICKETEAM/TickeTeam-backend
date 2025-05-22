package com.tickeTeam.domain.sectionPrice.dto.request;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SectionPriceRequest {

    private String sectionName;
    private int seatCount;
    private LocalDate matchDay;
}
