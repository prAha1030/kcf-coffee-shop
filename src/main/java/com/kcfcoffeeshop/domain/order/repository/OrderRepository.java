package com.kcfcoffeeshop.domain.order.repository;

import com.kcfcoffeeshop.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
