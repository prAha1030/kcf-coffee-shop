package com.kcfcoffeeshop.domain.order.service;

import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.menu.entity.Menu;
import com.kcfcoffeeshop.domain.menu.enums.MenuErrorCode;
import com.kcfcoffeeshop.domain.menu.repository.MenuRepository;
import com.kcfcoffeeshop.domain.order.dto.kafka.OrderCompleteEvent;
import com.kcfcoffeeshop.domain.order.dto.request.OrderCreateRequest;
import com.kcfcoffeeshop.domain.order.dto.response.OrderCreateResponse;
import com.kcfcoffeeshop.domain.order.entity.Order;
import com.kcfcoffeeshop.domain.order.entity.OrderItem;
import com.kcfcoffeeshop.domain.order.enums.OrderErrorCode;
import com.kcfcoffeeshop.domain.order.producer.OrderEventProducer;
import com.kcfcoffeeshop.domain.order.repository.OrderItemRepository;
import com.kcfcoffeeshop.domain.order.repository.OrderRepository;
import com.kcfcoffeeshop.domain.payment.entity.Payment;
import com.kcfcoffeeshop.domain.payment.repository.PaymentRepository;
import com.kcfcoffeeshop.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_LOCK_PREFIX = "order:lock:";
    private static final String ORDER_COUNTER_PREFIX = "order:counter:";
    private static final String PAYMENT_COUNTER_PREFIX = "payment:counter:";

    private final PointService pointService;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        // 메뉴 ID로 메뉴 조회
        List<Long> menuIds = request.items().stream()
                .map(OrderCreateRequest.OrderItemCreateRequest::menuId)
                .toList();
        List<Menu> menus = menuRepository.findAllById(menuIds);
        if (menus.size() != menuIds.size()) {
            throw new BusinessException(MenuErrorCode.ERR_NOT_FOUND);
        }
        // 총 금액 계산
        Map<Long, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));
        BigDecimal totalPrice = request.items().stream()
                .map(item -> menuMap.get(item.menuId())
                        .getPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 분산 락 획득
        String key = ORDER_LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(key);
        try {
            boolean isAcquired = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!isAcquired) {
                throw new BusinessException(OrderErrorCode.ERR_LOCK_FAIL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(OrderErrorCode.ERR_LOCK_FAIL);
        }
        try {
            // Order 생성
            String orderNumber = generateOrderNumber();
            Order order = Order.create(userId, orderNumber, totalPrice);
            Order savedOrder = orderRepository.save(order);
            // OrderItem 생성
            List<OrderItem> orderItems = request.items().stream()
                    .map(item -> OrderItem.create(
                            savedOrder.getId(),
                            menuMap.get(item.menuId()),
                            item.quantity()))
                    .toList();
            orderItemRepository.saveAll(orderItems);
            // Payment 생성
            String paymentNumber = generatePaymentNumber();
            Payment payment = Payment.create(savedOrder.getId(), paymentNumber, totalPrice);
            Payment savedPayment = paymentRepository.save(payment);
            // Point 차감 + PointLog 기록
            BigDecimal balance = pointService.deductPoint(userId, totalPrice);
            // 주문 + 결제 성공!
            savedOrder.complete();
            savedPayment.complete();
            // Kafka 주문 이벤트 전송
            orderItems.forEach(
                    orderItem -> orderEventProducer.sendOrderCompleteEvent(
                            OrderCompleteEvent.from(savedOrder.getOrderNumber(), orderItem)
                    )
            );

            return OrderCreateResponse.from(savedOrder.getOrderNumber(), savedPayment, balance);
        } finally {
            // 락 해제 (락을 획득했을 경우)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String generateOrderNumber() {
        return generateNumber(ORDER_COUNTER_PREFIX);
    }

    private String generatePaymentNumber() {
        return generateNumber(PAYMENT_COUNTER_PREFIX);
    }

    // 주문, 결제 번호 생성
    private String generateNumber(String counterPrefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String counterKey = counterPrefix + date;
        Long sequence = redisTemplate.opsForValue().increment(counterKey);
        redisTemplate.expire(counterKey, 1, TimeUnit.DAYS);
        String prefix = counterPrefix.equals(ORDER_COUNTER_PREFIX) ? "ORD" : "PAY";
        return String.format("%s-%s-%06d", prefix, date, sequence);
    }
}
