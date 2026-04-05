package com.kcfcoffeeshop.domain.order.repository;

import com.kcfcoffeeshop.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
