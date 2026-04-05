package com.kcfcoffeeshop.common.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    // producer 설정
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        // 서버 위치
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        // key, value 문자열로 직렬화
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 파티션 균등 분배 round robin
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());
        // producer 멱등성 (활성화 시 ack = all)
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // 재시도 (횟수, 간격, 전체 타임아웃)
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // 3회
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // 1초
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000); // 30초

        return new DefaultKafkaProducerFactory<>(props);
    }

    // producer가 사용할 kafkaTemplate
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }
}
