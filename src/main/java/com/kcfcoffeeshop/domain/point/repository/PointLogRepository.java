package com.kcfcoffeeshop.domain.point.repository;

import com.kcfcoffeeshop.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
}
