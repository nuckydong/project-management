package com.pm.service;

import com.pm.dto.request.PasswordChangeRequest;
import com.pm.dto.request.UserUpdateRequest;
import com.pm.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UserUpdateRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    UserResponse updateAvatar(Long userId, MultipartFile file);

    UserResponse getUserById(Long userId);
}
