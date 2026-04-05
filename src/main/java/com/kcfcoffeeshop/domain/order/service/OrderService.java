package com.kcfcoffeeshop.domain.order.service;

import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.menu.entity.Menu;
import com.kcfcoffeeshop.domain.menu.enums.MenuErrorCode;
import com.kcfcoffeeshop.domain.menu.repository.MenuRepository;
import com.kcfcoffeeshop.domain.order.dto.request.OrderCreateRequest;
import com.kcfcoffeeshop.domain.order.dto.response.OrderCreateResponse;
import com.kcfcoffeeshop.domain.order.enums.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_LOCK_PREFIX = "order:lock:";

    private final MenuRepository menuRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        // 메뉴 ID로 DB에서 가격 조회
        List<Long> menuIds = request.items().stream()
                .map(OrderCreateRequest.OrderItemCreateRequest::menuId)
                .toList();
        List<Menu> menus = menuRepository.findAllById(menuIds);
        if (menus.size() != menuIds.size()) {
            throw new BusinessException(MenuErrorCode.ERR_NOT_FOUND);
        }
        // 총 금액 계산 -> 포인트 잔액 비교
        Map<Long, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));
        BigDecimal totalPrice = request.items().stream()
                .map(item -> menuMap.get(item.menuId())
                        .getPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 분산 락 획득
        String key = ORDER_LOCK_PREFIX + userId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "locked", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(OrderErrorCode.ERR_LOCK_FAIL);
        }
        try {
            // Order + OrderItem 생성
            // Payment 생성
            // Point 차감 + PointLog 기록
        } finally {
            // 락 해제
            redisTemplate.delete(key);
        }
        // Kafka 주문 이벤트 전송
        // return
    }
}
