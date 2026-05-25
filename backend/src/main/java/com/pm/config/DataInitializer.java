package com.pm.config;

import com.pm.entity.User;
import com.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
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
