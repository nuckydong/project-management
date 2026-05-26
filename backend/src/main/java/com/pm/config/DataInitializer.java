package com.pm.config;

import com.pm.entity.User;
import com.pm.repository.UserRepository;
import com.pm.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;

    @Override
    public void run(String... args) {
        initMinioBuckets();
        initAdminUser();
    }

    private void initMinioBuckets() {
        try {
            minioService.initBuckets();
            log.info("MinIO 存储桶初始化完成");
        } catch (Exception e) {
            log.warn("MinIO 存储桶初始化失败（MinIO 可能未启动）: {}", e.getMessage());
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@project-management.com");
            admin.setPassword(passwordEncoder.encode("admin149162536"));
            admin.setStatus((short) 1);
            userRepository.save(admin);
            log.info("超级管理员账户已初始化: admin");
        }
    }
}
