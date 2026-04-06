package com.kcfcoffeeshop.domain.point.controller;

import com.kcfcoffeeshop.common.dto.BaseResponse;
import com.kcfcoffeeshop.domain.point.dto.request.PointChargeRequest;
import com.kcfcoffeeshop.domain.point.dto.response.PointChargeResponse;
import com.kcfcoffeeshop.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<BaseResponse<PointChargeResponse>> chargePoint(
            @Valid @RequestBody PointChargeRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success(
                        HttpStatus.CREATED,
                        "포인트 충전 성공",
                        pointService.chargePoint(request, userId, idempotencyKey)
                )
        );
    }
}
