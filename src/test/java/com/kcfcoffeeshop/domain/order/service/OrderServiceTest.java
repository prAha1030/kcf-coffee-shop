package com.kcfcoffeeshop.domain.order.service;

import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.menu.entity.Menu;
import com.kcfcoffeeshop.domain.menu.repository.MenuRepository;
import com.kcfcoffeeshop.domain.order.dto.request.OrderCreateRequest;
import com.kcfcoffeeshop.domain.order.dto.response.OrderCreateResponse;
import com.kcfcoffeeshop.domain.order.entity.Order;
import com.kcfcoffeeshop.domain.order.producer.OrderEventProducer;
import com.kcfcoffeeshop.domain.order.repository.OrderItemRepository;
import com.kcfcoffeeshop.domain.order.repository.OrderRepository;
import com.kcfcoffeeshop.domain.payment.entity.Payment;
import com.kcfcoffeeshop.domain.payment.repository.PaymentRepository;
import com.kcfcoffeeshop.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PointService pointService;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private RLock rLock;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("정상 주문")
        void createOrder_success() throws InterruptedException {
            // given
            OrderCreateRequest request = new OrderCreateRequest(List.of(
                    new OrderCreateRequest.OrderItemCreateRequest(1L, 2)
            ));
            Menu menu = mock(Menu.class);
            when(menu.getId()).thenReturn(1L);
            when(menu.getPrice()).thenReturn(BigDecimal.valueOf(4500));
            when(menuRepository.findAllById(any())).thenReturn(List.of(menu));
            when(redissonClient.getLock(anyString())).thenReturn(rLock);
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
            when(rLock.isHeldByCurrentThread()).thenReturn(true);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(any())).thenReturn(1L);
            Order savedOrder = mock(Order.class);
            when(savedOrder.getId()).thenReturn(1L);
            when(savedOrder.getOrderNumber()).thenReturn("ORD-20260406-000001");
            when(orderRepository.save(any())).thenReturn(savedOrder);
            Payment savedPayment = mock(Payment.class);
            when(paymentRepository.save(any())).thenReturn(savedPayment);
            when(pointService.deductPoint(any(), any())).thenReturn(BigDecimal.valueOf(85500));

            // when
            OrderCreateResponse response = orderService.createOrder(request, 1L);

            // then
            assertNotNull(response);
            verify(orderRepository).save(any());
            verify(paymentRepository).save(any());
            verify(pointService).deductPoint(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 주문 시 예외 발생")
        void createOrder_menu_not_found() throws InterruptedException {
            // given
            OrderCreateRequest request = new OrderCreateRequest(List.of(
                    new OrderCreateRequest.OrderItemCreateRequest(999L, 2)
            ));
            when(menuRepository.findAllById(any())).thenReturn(List.of());

            // when & then
            assertThrows(BusinessException.class,
                    () -> orderService.createOrder(request, 1L));
        }

        @Test
        @DisplayName("락 획득 실패 시 예외 발생")
        void createOrder_lock_fail() throws InterruptedException {
            // given
            OrderCreateRequest request = new OrderCreateRequest(List.of(
                    new OrderCreateRequest.OrderItemCreateRequest(1L, 2)
            ));
            Menu menu = mock(Menu.class);
            when(menu.getId()).thenReturn(1L);
            when(menu.getPrice()).thenReturn(BigDecimal.valueOf(4500));
            when(menuRepository.findAllById(any())).thenReturn(List.of(menu));
            when(redissonClient.getLock(anyString())).thenReturn(rLock);
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

            // when & then
            assertThrows(BusinessException.class,
                    () -> orderService.createOrder(request, 1L));
        }
    }
}
