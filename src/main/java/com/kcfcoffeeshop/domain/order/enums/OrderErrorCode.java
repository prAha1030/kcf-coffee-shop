package com.kcfcoffeeshop.domain.order.enums;

import com.kcfcoffeeshop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ERR_LOCK_FAIL(HttpStatus.CONFLICT, "주문 처리 중입니다. 잠시 후 다시 시도해주세요");

    private final HttpStatus httpStatus;
    private final String message;
}
