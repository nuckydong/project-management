package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.PasswordChangeRequest;
import com.pm.dto.request.UserUpdateRequest;
import com.pm.dto.response.UserResponse;
import com.pm.service.UserService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/search")
    public Result<List<UserResponse>> search(@RequestParam(required = false) String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }

    @GetMapping("/list")
    public Result<List<UserResponse>> listAll() {
        return Result.success(userService.listAllUsers());
    }

    @PutMapping("/{id}/status")
    public Result<UserResponse> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Short> body) {
        return Result.success(userService.updateUserStatus(id, body.get("status")));
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword"));
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        return Result.success(userService.getProfile(securityUtil.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateProfile(securityUtil.getCurrentUserId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(securityUtil.getCurrentUserId(), request);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<UserResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.updateAvatar(securityUtil.getCurrentUserId(), file));
    }
}
