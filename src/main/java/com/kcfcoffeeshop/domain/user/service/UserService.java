package com.kcfcoffeeshop.domain.user.service;

import com.kcfcoffeeshop.domain.point.entity.Point;
import com.kcfcoffeeshop.domain.point.repository.PointRepository;
import com.kcfcoffeeshop.domain.user.dto.request.UserSignupRequest;
import com.kcfcoffeeshop.domain.user.dto.response.UserSignupResponse;
import com.kcfcoffeeshop.domain.user.entity.User;
import com.kcfcoffeeshop.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponse userSignup(UserSignupRequest request) {

        // TODO 이메일 중복 검증

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
}
