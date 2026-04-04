package com.kcfcoffeeshop.domain.order.dto.response;

import com.kcfcoffeeshop.domain.order.entity.Order;
import com.kcfcoffeeshop.domain.payment.entity.Payment;
import com.kcfcoffeeshop.domain.point.entity.Point;

import java.math.BigDecimal;

public record OrderCreateResponse(
        String orderNumber,
        String paymentNumber,
        BigDecimal totalAmount,
        BigDecimal balance
) {
    public static OrderCreateResponse from(Order order, Payment payment, Point point) {
        return new OrderCreateResponse(
                order.getOrderNumber(),
                payment.getPaymentNumber(),
                payment.getTotalAmount(),
                point.getBalance()
        );
    }
}
