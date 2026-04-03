package com.kcfcoffeeshop.domain.user.service;

import com.kcfcoffeeshop.common.config.security.JwtUtil;
import com.kcfcoffeeshop.common.exception.BusinessException;
import com.kcfcoffeeshop.domain.point.entity.Point;
import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import com.kcfcoffeeshop.domain.user.dto.request.UserLoginRequest;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import com.kcfcoffeeshop.domain.user.dto.response.UserLoginResponse;
import com.kcfcoffeeshop.domain.user.dto.response.UserSignupResponse;
import com.kcfcoffeeshop.domain.user.entity.User;
import com.kcfcoffeeshop.domain.user.enums.UserErrorCode;
import com.kcfcoffeeshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public UserSignupResponse userSignup(UserSignupRequest request) {

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(UserErrorCode.ERR_DUPLICATED_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 유저 생성 후 DB 저장
        User user = User.create(request, encodedPassword);
        User savedUser = userRepository.save(user);

        // 생성된 유저의 포인트 생성 후 DB 저장
        Point point = Point.create(savedUser.getId());
        pointRepository.save(point);

        return UserSignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserLoginResponse userLogin(UserLoginRequest request) {
        // 이메일 & 비밀번호 검증
        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new BusinessException(UserErrorCode.ERR_NOT_FOUND)
        );
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.ERR_INVALID_PASSWORD);
        }

        // 토큰 발급
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken();

        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        return new UserLoginResponse(accessToken, refreshToken);
    }
}
