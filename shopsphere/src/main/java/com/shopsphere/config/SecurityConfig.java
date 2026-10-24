package com.shopsphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            HttpSessionCsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401, "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403, "Access denied")))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
