package com.kcfcoffeeshop.domain.order.dto.response;

import com.kcfcoffeeshop.domain.payment.entity.Payment;

import java.math.BigDecimal;

public record OrderCreateResponse(
        String orderNumber,
        String paymentNumber,
        BigDecimal totalAmount,
        BigDecimal balance
) {
    public static OrderCreateResponse from(String orderNumber, Payment payment, BigDecimal balance) {
        return new OrderCreateResponse(
                orderNumber,
                payment.getPaymentNumber(),
                payment.getTotalAmount(),
                balance
        );
    }
}
