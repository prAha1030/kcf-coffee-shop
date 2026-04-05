package com.kcfcoffeeshop.domain.order.entity;

import com.kcfcoffeeshop.common.entity.BaseEntity;
import com.kcfcoffeeshop.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    public static Order create(Long userId, String orderNumber, BigDecimal totalPrice) {
        Order order = new Order();
        order.userId = userId;
        order.orderNumber = orderNumber;
        order.totalPrice = totalPrice;
        order.status = OrderStatus.PENDING;
        return order;
    }
}
