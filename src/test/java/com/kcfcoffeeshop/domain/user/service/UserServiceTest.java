package com.kcfcoffeeshop.domain.user.service;

import com.kcfcoffeeshop.common.config.security.JwtUtil;
import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import com.kcfcoffeeshop.domain.user.dto.request.UserLoginRequest;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import com.kcfcoffeeshop.domain.user.dto.response.UserLoginResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

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

        @Test
        @DisplayName("이메일 중복 시 예외 발생")
        void signup_duplicated_email() {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    "테스트", "test@test.com", "password123!", "010-1234-5678", "서울시"
            );
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            // when & then
            assertThrows(BusinessException.class, () -> userService.userSignup(request));
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인")
        void login_success() {
            // given
            UserLoginRequest request = new UserLoginRequest("test@test.com", "password123!");
            User user = mock(User.class);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
            when(jwtUtil.createAccessToken(any(), any(), any())).thenReturn("accessToken");
            when(jwtUtil.createRefreshToken()).thenReturn("refreshToken");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when
            UserLoginResponse response = userService.userLogin(request);

            // then
            assertNotNull(response);
            assertEquals("accessToken", response.accessToken());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_user_not_found() {
            // given
            UserLoginRequest request = new UserLoginRequest("test@test.com", "password123!");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            // when & then
            assertThrows(BusinessException.class, () -> userService.userLogin(request));
        }

        @Test
        @DisplayName("비밀번호 불일치 시 예외 발생")
        void login_invalid_password() {
            // given
            UserLoginRequest request = new UserLoginRequest("test@test.com", "wrongPassword");
            User user = mock(User.class);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

            // when & then
            assertThrows(BusinessException.class, () -> userService.userLogin(request));
        }
    }
}