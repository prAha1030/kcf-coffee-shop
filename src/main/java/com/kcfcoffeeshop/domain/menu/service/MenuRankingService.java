package com.kcfcoffeeshop.domain.menu.service;

import com.kcfcoffeeshop.common.config.kafka.KafkaConstants;
import com.kcfcoffeeshop.domain.order.dto.kafka.OrderCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuRankingService {

    private static final String RANKING_KEY_PREFIX = "menu:ranking:";

    private final RedisTemplate<String, String> redisTemplate;

    // 메뉴 랭킹 점수 증가 (판매 수 기준, 집계 기준 : 일주일)
    public void increaseMenuRanking(OrderCompleteEvent event) {
        // 멱등성 키로 중복 체크
        String idempotentKey = KafkaConstants.ORDER_PROCESSED_PREFIX + event.orderNumber() + ":" + event.menuId();
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "processed", 1, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(isNew)) {
            log.info("중복 주문 성공 이벤트 스킵 - 이미 처리된 주문 : {}", idempotentKey);
            return;
        }
        // 메뉴 랭킹 업데이트
        String rankingKey = RANKING_KEY_PREFIX + Year.now() + ":W:" + LocalDate.now().get(WeekFields.ISO.weekOfYear());
        redisTemplate.opsForZSet().incrementScore(rankingKey, String.valueOf(event.menuId()), event.quantity());
        if (redisTemplate.getExpire(rankingKey) == -1L) {
            redisTemplate.expire(rankingKey, 14, TimeUnit.DAYS);
        }
    }
}
