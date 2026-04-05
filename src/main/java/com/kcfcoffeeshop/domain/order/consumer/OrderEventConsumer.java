package com.kcfcoffeeshop.domain.order.consumer;

import com.kcfcoffeeshop.common.config.kafka.KafkaConstants;
import com.kcfcoffeeshop.domain.menu.service.MenuRankingService;
import com.kcfcoffeeshop.domain.order.dto.kafka.OrderCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final MenuRankingService menuRankingService;

    @KafkaListener(
            topics = KafkaConstants.ORDER_COMPLETE_TOPIC,
            groupId = KafkaConstants.ORDER_GROUP_ID,
            containerFactory = KafkaConstants.STRING_LISTENER_FACTORY
    )
    public void consumeOrderCompleteEvent(String message) {
        // checked exception은 DLT 처리
        OrderCompleteEvent event = objectMapper.readValue(message, new TypeReference<>() {});
        log.info("Kafka 주문 성공 이벤트 수신 성공 : {}", event);
        // 메뉴 랭킹 업데이트
        menuRankingService.increaseMenuRanking(event);
    }
}
