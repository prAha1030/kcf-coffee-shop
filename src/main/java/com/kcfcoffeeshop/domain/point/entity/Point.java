package com.kcfcoffeeshop.domain.point.entity;

import com.kcfcoffeeshop.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal balance;

    public static Point create(Long userId) {
        Point point = new Point();
        point.userId = userId;
        point.balance = BigDecimal.ZERO;
        return point;
    }

    public void charge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void deduct(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
