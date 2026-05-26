package com.pm.service;

import com.pm.dto.request.PasswordChangeRequest;
import com.pm.dto.request.UserUpdateRequest;
import com.pm.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<UserResponse> searchUsers(String keyword);

    List<UserResponse> listAllUsers();

    UserResponse createUser(String username, String email, String password);

    UserResponse updateUser(Long userId, String username, String email);

    void deleteUser(Long userId);

    UserResponse updateUserStatus(Long userId, Short status);

    void resetPassword(Long userId, String newPassword);

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UserUpdateRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    UserResponse updateAvatar(Long userId, MultipartFile file);

    UserResponse getUserById(Long userId);
}
