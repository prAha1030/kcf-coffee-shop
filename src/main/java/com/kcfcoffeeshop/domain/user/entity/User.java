package com.kcfcoffeeshop.domain.user.entity;

import com.kcfcoffeeshop.common.entity.BaseEntity;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    private String address;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public static User create(UserSignupRequest request, String encodedPassword) {
        User user = new User();
        user.name = request.name();
        user.email = request.email();
        user.password = encodedPassword;
        user.phoneNumber = request.phoneNumber();
        user.address = request.address();
        return user;
    }
}
