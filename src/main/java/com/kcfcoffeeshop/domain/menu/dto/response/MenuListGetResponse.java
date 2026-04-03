package com.kcfcoffeeshop.domain.menu.dto.response;

import com.kcfcoffeeshop.domain.menu.entity.Menu;
import com.kcfcoffeeshop.domain.menu.enums.MenuStatus;

import java.math.BigDecimal;

public record MenuListGetResponse(
        Long menuId,
        String name,
        BigDecimal price,
        MenuStatus status
) {
    public static MenuListGetResponse from(Menu menu) {
        return new MenuListGetResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStatus()
        );
    }
}
