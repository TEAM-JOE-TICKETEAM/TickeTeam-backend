package com.tickeTeam.domain.sectionPrice.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.sectionPrice.dto.request.SectionPriceRequest;
import com.tickeTeam.domain.sectionPrice.service.SectionPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/section-price")
@RequiredArgsConstructor
public class SectionPriceController {

    private final SectionPriceService sectionPriceService;

    @GetMapping
    public ResponseEntity<ResultResponse> checkSectionPrice(@RequestBody SectionPriceRequest sectionPriceRequest){
        return ResponseEntity.ok(sectionPriceService.getSectionPrice(sectionPriceRequest));
    }
}
