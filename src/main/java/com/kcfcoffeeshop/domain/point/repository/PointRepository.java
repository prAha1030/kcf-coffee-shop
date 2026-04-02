package com.kcfcoffeeshop.domain.point.repository;

import com.kcfcoffeeshop.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
