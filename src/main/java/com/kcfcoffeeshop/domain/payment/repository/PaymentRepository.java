package com.kcfcoffeeshop.domain.payment.repository;

import com.kcfcoffeeshop.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
