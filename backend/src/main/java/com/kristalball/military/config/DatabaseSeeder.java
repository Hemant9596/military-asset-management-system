package com.kristalball.military.config;

import com.kristalball.military.entity.RoleName;
import com.kristalball.military.entity.User;
import com.kristalball.military.repository.RoleRepository;
import com.kristalball.military.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${INITIAL_ADMIN_EMAIL:}")
    private String initialAdminEmail;

    @Value("${INITIAL_ADMIN_PASSWORD:}")
    private String initialAdminPassword;

    public DatabaseSeeder(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (initialAdminEmail.isBlank() && initialAdminPassword.isBlank()) {
            return;
        }
        if (initialAdminEmail.isBlank() || initialAdminPassword.length() < 12) {
            throw new IllegalStateException(
                    "Set INITIAL_ADMIN_EMAIL and an INITIAL_ADMIN_PASSWORD of at least 12 characters together");
        }
        initialAdminEmail = initialAdminEmail.trim().toLowerCase();
        if (userRepository.findByEmail(initialAdminEmail).isPresent()) {
            return;
        }

        var adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role is missing from the Flyway schema"));
        userRepository.save(User.builder()
                .firstName("System")
                .lastName("Administrator")
                .email(initialAdminEmail)
                .password(passwordEncoder.encode(initialAdminPassword))
                .role(adminRole)
                .enabled(true)
                .build());
    }
}