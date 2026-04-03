package com.kcfcoffeeshop.domain.point.dto.response;

import com.kcfcoffeeshop.domain.point.entity.Point;

import java.math.BigDecimal;

public record PointChargeResponse(
        Long pointId,
        BigDecimal balance
) {
    public static PointChargeResponse from(Point point) {
        return new PointChargeResponse(
                point.getId(),
                point.getBalance()
        );
    }
}
