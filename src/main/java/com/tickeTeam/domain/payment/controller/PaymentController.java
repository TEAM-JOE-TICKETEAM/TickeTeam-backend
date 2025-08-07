package com.tickeTeam.domain.payment.controller;

import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.payment.dto.request.PaymentRequest;
import com.tickeTeam.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ResultResponse> pay(@RequestBody PaymentRequest request){
        return ResponseEntity.ok(ResultResponse.of(ResultCode.PAYMENT_SUCCESS,paymentService.pay(request)));
    }
}
