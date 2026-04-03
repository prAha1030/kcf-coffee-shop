package com.kcfcoffeeshop.domain.menu.controller;

import com.kcfcoffeeshop.common.dto.BaseResponse;
import com.kcfcoffeeshop.common.dto.PageResponse;
import com.kcfcoffeeshop.domain.menu.dto.response.MenuListGetResponse;
import com.kcfcoffeeshop.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<MenuListGetResponse>>> getMenuList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(HttpStatus.OK, "메뉴 목록 조회 성공", menuService.getMenuList(pageable))
        );
    }
}
