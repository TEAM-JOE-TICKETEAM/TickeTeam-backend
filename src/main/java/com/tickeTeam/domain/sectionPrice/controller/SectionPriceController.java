package com.tickeTeam.domain.sectionPrice.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.sectionPrice.dto.request.SectionPriceRequest;
import com.tickeTeam.domain.sectionPrice.service.SectionPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/section-price")
@RequiredArgsConstructor
@Tag(name = "SectionPriceController", description = "좌석 구역별 가격 정보 API")
public class SectionPriceController {

    private final SectionPriceService sectionPriceService;

    @Operation(
            summary = "좌석 구역 가격 정보 조회",
            description = "좌석 이 소속된 구역의 가격 정보를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ResultResponse> checkSectionPrice(@RequestBody SectionPriceRequest sectionPriceRequest){
        return ResponseEntity.ok(sectionPriceService.getSectionPrice(sectionPriceRequest));
    }
}
