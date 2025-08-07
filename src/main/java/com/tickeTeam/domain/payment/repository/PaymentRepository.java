package com.tickeTeam.domain.payment.repository;

import com.tickeTeam.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
