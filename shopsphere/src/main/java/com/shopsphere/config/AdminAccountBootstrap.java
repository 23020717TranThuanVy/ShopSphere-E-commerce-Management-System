package com.shopsphere.config;

import com.shopsphere.entity.AccountRole;
import com.shopsphere.entity.AccountUser;
import com.shopsphere.repository.AccountUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountBootstrap {

    @Bean
    ApplicationRunner createBootstrapAdmin(
            AccountUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${shopsphere.bootstrap-admin.email:}") String email,
            @Value("${shopsphere.bootstrap-admin.password:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) {
                return;
            }
            if (email.isBlank() || password.length() < 12) {
                throw new IllegalStateException(
                        "Set both bootstrap admin credentials; password must be at least 12 characters");
            }

            String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
            if (!users.existsByEmailIgnoreCase(normalizedEmail)) {
                users.save(new AccountUser(
                        "ShopSphere Admin",
                        normalizedEmail,
                        passwordEncoder.encode(password),
                        AccountRole.ADMIN));
            }
        };
    }
}
