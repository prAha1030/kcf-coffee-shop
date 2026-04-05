package com.kcfcoffeeshop.domain.menu.dto.response;

import com.kcfcoffeeshop.domain.menu.entity.Menu;
import com.kcfcoffeeshop.domain.menu.enums.MenuStatus;

import java.math.BigDecimal;

public record MenuBestResponse(
        int rank,
        Long menuId,
        String name,
        BigDecimal price,
        MenuStatus status,
        double orderCount
) {
    public static MenuBestResponse from(int rank, Menu menu, double orderCount) {
        return new MenuBestResponse(
                rank,
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStatus(),
                orderCount
        );
    }
}
