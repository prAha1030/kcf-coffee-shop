package com.kcfcoffeeshop.domain.user.enums;

import com.kcfcoffeeshop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    ERR_DUPLICATED_EMAIL(HttpStatus.CONFLICT, "중복된 이메일이 존재합니다"),
    ERR_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    ERR_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
