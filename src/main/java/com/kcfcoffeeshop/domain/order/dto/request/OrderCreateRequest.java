package com.kcfcoffeeshop.domain.order.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty(message = "주문 상품은 필수입니다")
        List<OrderItemCreateRequest> items
) {
    public record OrderItemCreateRequest(

            @NotNull(message = "메뉴 ID는 필수입니다")
            Long menuId,

            @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
            @Max(value = 100, message = "수량은 100개 이하여야 합니다")
            int quantity
    ) {
    }
}
