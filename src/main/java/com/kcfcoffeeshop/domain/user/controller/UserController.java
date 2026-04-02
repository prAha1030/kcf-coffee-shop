package com.kcfcoffeeshop.domain.user.controller;

import com.kcfcoffeeshop.common.dto.BaseResponse;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import com.kcfcoffeeshop.domain.user.dto.response.UserSignupResponse;
import com.kcfcoffeeshop.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<UserSignupResponse>> userSignup(@Valid @RequestBody UserSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success(HttpStatus.CREATED, "회원가입 성공", userService.userSignup(request))
        );
    }
}
