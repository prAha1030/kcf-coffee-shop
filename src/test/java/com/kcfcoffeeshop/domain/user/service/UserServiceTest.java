package com.kcfcoffeeshop.domain.user.service;

import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import com.kcfcoffeeshop.domain.user.dto.response.UserSignupResponse;
import com.kcfcoffeeshop.domain.user.entity.User;
import com.kcfcoffeeshop.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("정상 회원가입")
        void signup_success() {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    "테스트", "test@test.com", "password123!", "010-1234-5678", "서울시"
            );
            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            User savedUser = User.create(request, "encodedPassword");
            when(userRepository.save(any())).thenReturn(savedUser);

            // when
            UserSignupResponse response = userService.userSignup(request);

            // then
            assertNotNull(response);
            verify(userRepository).save(any());
            verify(pointRepository).save(any());
        }
    }
}