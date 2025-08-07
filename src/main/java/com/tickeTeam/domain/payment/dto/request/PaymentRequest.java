package com.tickeTeam.domain.payment.dto.request;

import java.util.List;

public record PaymentRequest(
        List<Long> seatIds,
        int totalPrice,
        String email,
        String paymentMethod,
        String paymentStatus
) {
}
