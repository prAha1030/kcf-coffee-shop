package com.kcfcoffeeshop.domain.order.dto.kafka;

import com.kcfcoffeeshop.domain.order.entity.OrderItem;

public record OrderCompleteEvent(
        String orderNumber,
        Long menuId,
        int quantity
) {
    public static OrderCompleteEvent from(String orderNumber, OrderItem orderItem) {
        return new OrderCompleteEvent(
                orderNumber,
                orderItem.getMenuId(),
                orderItem.getQuantity()
        );
    }
}
