package com.kcfcoffeeshop.domain.user.repository;

import com.kcfcoffeeshop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
