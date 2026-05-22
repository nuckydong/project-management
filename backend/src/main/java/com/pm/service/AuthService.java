package com.pm.service;

import com.pm.dto.request.LoginRequest;
import com.pm.dto.request.RegisterRequest;
import com.pm.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(Long userId);
}
