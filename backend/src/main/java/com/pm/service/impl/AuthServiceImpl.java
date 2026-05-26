package com.pm.service.impl;

import com.pm.common.Constants;
import com.pm.dto.request.LoginRequest;
import com.pm.dto.request.RegisterRequest;
import com.pm.dto.response.AuthResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.User;
import com.pm.repository.UserRepository;
import com.pm.security.JwtTokenProvider;
import com.pm.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (user.getStatus() != Constants.UserStatus.ACTIVE.getValue()) {
            throw new RuntimeException("Account is not active");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        storeTokens(user.getId(), accessToken, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus((short) Constants.UserStatus.ACTIVE.getValue());
        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        storeTokens(user.getId(), accessToken, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        String storedToken = redisTemplate.opsForValue().get(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token not found or expired");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        redisTemplate.opsForValue().set(
                Constants.REDIS_TOKEN_PREFIX + userId,
                newAccessToken,
                7, TimeUnit.DAYS
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete(Constants.REDIS_TOKEN_PREFIX + userId);
        redisTemplate.delete(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
    }

    private void storeTokens(Long userId, String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set(Constants.REDIS_TOKEN_PREFIX + userId, accessToken, 7, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId, refreshToken, 30, TimeUnit.DAYS);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
