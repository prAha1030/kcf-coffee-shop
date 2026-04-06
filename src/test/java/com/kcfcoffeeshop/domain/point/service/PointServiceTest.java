package com.kcfcoffeeshop.domain.point.service;

import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.point.dto.request.PointChargeRequest;
import com.kcfcoffeeshop.domain.point.dto.response.PointChargeResponse;
import com.kcfcoffeeshop.domain.point.entity.Point;
import com.kcfcoffeeshop.domain.point.repository.PointLogRepository;
import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;
    @Mock
    private PointLogRepository pointLogRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PointService pointService;

    @Nested
    @DisplayName("포인트 충전")
    class Charge {

        @Test
        @DisplayName("정상 충전")
        void charge_success() {
            // given
            PointChargeRequest request = new PointChargeRequest(BigDecimal.valueOf(10000));
            String idempotencyKey = UUID.randomUUID().toString();
            Point point = Point.create(1L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(any(), any(), anyLong(), any())).thenReturn(true);
            when(pointRepository.findByUserId(1L)).thenReturn(Optional.of(point));

            // when
            PointChargeResponse response = pointService.chargePoint(request, 1L, idempotencyKey);

            // then
            assertEquals(BigDecimal.valueOf(10000), response.balance());
            verify(pointLogRepository).save(any());
        }

        @Test
        @DisplayName("중복 요청 시 기존 결과 반환")
        void charge_duplicate_request() {
            // given
            PointChargeRequest request = new PointChargeRequest(BigDecimal.valueOf(10000));
            String idempotencyKey = UUID.randomUUID().toString();
            Point point = Point.create(1L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(any(), any(), anyLong(), any())).thenReturn(false);
            when(pointRepository.findByUserId(1L)).thenReturn(Optional.of(point));

            // when
            PointChargeResponse response = pointService.chargePoint(request, 1L, idempotencyKey);

            // then
            assertEquals(BigDecimal.ZERO, response.balance());
            verify(pointLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("포인트 차감")
    class Deduct {

        @Test
        @DisplayName("정상 차감")
        void deduct_success() {
            // given
            Point point = Point.create(1L);
            point.charge(BigDecimal.valueOf(10000));
            when(pointRepository.findByUserId(1L)).thenReturn(Optional.of(point));

            // when
            BigDecimal balance = pointService.deductPoint(1L, BigDecimal.valueOf(5000));

            // then
            assertEquals(BigDecimal.valueOf(5000), balance);
            verify(pointLogRepository).save(any());
        }

        @Test
        @DisplayName("잔액 부족 시 예외 발생")
        void deduct_insufficient_balance() {
            // given
            Point point = Point.create(1L);
            point.charge(BigDecimal.valueOf(1000));
            when(pointRepository.findByUserId(1L)).thenReturn(Optional.of(point));

            // when & then
            assertThrows(BusinessException.class,
                    () -> pointService.deductPoint(1L, BigDecimal.valueOf(5000)));
        }
    }
}
