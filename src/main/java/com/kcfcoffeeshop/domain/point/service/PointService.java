package com.kcfcoffeeshop.domain.point.service;

import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.point.dto.request.PointChargeRequest;
import com.kcfcoffeeshop.domain.point.dto.response.PointChargeResponse;
import com.kcfcoffeeshop.domain.point.entity.Point;
import com.kcfcoffeeshop.domain.point.entity.PointLog;
import com.kcfcoffeeshop.domain.point.enums.PointErrorCode;
import com.kcfcoffeeshop.domain.point.repository.PointLogRepository;
import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;

    @Transactional
    public PointChargeResponse chargePoint(PointChargeRequest request, Long userId) {
        // 포인트 충전
        Point point = pointRepository.findByUserId(userId).orElseThrow(
                () -> new BusinessException(PointErrorCode.ERR_NOT_FOUND)
        );
        point.charge(request.chargeAmount());
        // 포인트 이력 생성 및 DB 저장
        PointLog pointLog = PointLog.byCharge(point.getId(), request.chargeAmount());
        pointLogRepository.save(pointLog);

        return PointChargeResponse.from(point);
    }

    @Transactional
    public BigDecimal deductPoint(Long userId, BigDecimal amount) {
        // 포인트 차감
        Point point = pointRepository.findByUserId(userId).orElseThrow(
                () -> new BusinessException(PointErrorCode.ERR_NOT_FOUND)
        );
        if (point.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(PointErrorCode.ERR_INSUFFICIENT_BALANCE);
        }
        point.deduct(amount);
        // 포인트 이력 생성 및 DB 저장
        PointLog pointLog = PointLog.byPayment(point.getId(), amount);
        pointLogRepository.save(pointLog);

        return point.getBalance();
    }
}
