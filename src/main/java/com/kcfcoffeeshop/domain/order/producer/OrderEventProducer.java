package com.kcfcoffeeshop.domain.order.producer;

import com.kcfcoffeeshop.domain.order.dto.kafka.OrderCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String COMPLETE_TOPIC = "order.complete";

    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCompleteEvent(OrderCompleteEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            stringKafkaTemplate.send(COMPLETE_TOPIC, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Kafka 주문 성공 이벤트 전송 실패 : {}", ex.getMessage());
                            // TODO 실패 이력 DB 저장 또는 알림 발송 추가 예정
                        } else {
                            log.info("Kafka 주문 성공 이벤트 전송 성공 : {}", message);
                        }
                    });
        } catch (Exception e) {
            log.error("Kafka 주문 성공 이벤트 직렬화 실패 : {}", e.getMessage());
        }
    }
}
