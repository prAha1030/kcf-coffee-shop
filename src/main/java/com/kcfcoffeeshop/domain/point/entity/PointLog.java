package com.kcfcoffeeshop.domain.point.entity;

import com.kcfcoffeeshop.common.entity.BaseEntity;
import com.kcfcoffeeshop.domain.point.enums.PointLogStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "point_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pointId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointLogStatus status;

    public static PointLog byCharge(Long pointId, BigDecimal amount) {
        PointLog pointLog = new PointLog();
        pointLog.pointId = pointId;
        pointLog.amount = amount;
        pointLog.status = PointLogStatus.CHARGE;
        return pointLog;
    }

    public static PointLog byPayment(Long pointId, BigDecimal amount) {
        PointLog pointLog = new PointLog();
        pointLog.pointId = pointId;
        pointLog.amount = amount;
        pointLog.status = PointLogStatus.PAYMENT;
        return pointLog;
    }
}
