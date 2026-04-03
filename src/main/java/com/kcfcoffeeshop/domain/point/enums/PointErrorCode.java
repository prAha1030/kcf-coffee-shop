package com.kcfcoffeeshop.domain.point.enums;

import com.kcfcoffeeshop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
    ERR_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포인트입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
