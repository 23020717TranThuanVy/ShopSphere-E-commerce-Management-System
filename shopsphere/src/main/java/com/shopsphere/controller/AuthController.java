package com.shopsphere.controller;

import com.shopsphere.dto.AccountResponse;
import com.shopsphere.dto.LoginRequest;
import com.shopsphere.dto.RegisterRequest;
import com.shopsphere.entity.AccountRole;
import com.shopsphere.entity.AccountStatus;
import com.shopsphere.entity.AccountUser;
import com.shopsphere.repository.AccountUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountUserRepository accountUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AccountUserRepository accountUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.accountUserRepository = accountUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        String email = normalizeEmail(request.getEmail());
        if (accountUserRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("An account with this email already exists"));
        }

        AccountRole requestedRole;
        try {
            requestedRole = AccountRole.valueOf(request.getAccountType().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new MessageResponse("Choose Customer or Seller account type"));
        }
        if (requestedRole != AccountRole.CUSTOMER && requestedRole != AccountRole.SELLER) {
            return ResponseEntity.badRequest().body(new MessageResponse("Choose Customer or Seller account type"));
        }
        String displayName = email.substring(0, email.indexOf('@'));
        displayName = displayName.substring(0, Math.min(displayName.length(), 120));

        AccountUser account = accountUserRepository.save(new AccountUser(
                displayName,
                email,
                passwordEncoder.encode(request.getPassword()),
                requestedRole,
                requestedRole == AccountRole.SELLER ? AccountStatus.PENDING : AccountStatus.ACTIVE,
                request.getStoreName() == null || request.getStoreName().isBlank() ? null : request.getStoreName().trim(),
                requestedRole == AccountRole.CUSTOMER));
        if (requestedRole == AccountRole.SELLER) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new MessageResponse("Seller application submitted. You can sign in after Admin approval."));
        }
        return authenticate(email, request.getPassword(), servletRequest, servletResponse, account);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        String email = normalizeEmail(request.getEmail());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid email or password"));
        }
        AccountUser account = accountUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthenticationServiceException("Account was not found"));
        return saveAuthentication(authentication, servletRequest, servletResponse, account);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Authentication required"));
        }
        return accountUserRepository.findByEmailIgnoreCase(authentication.getName())
                .<ResponseEntity<?>>map(account -> ResponseEntity.ok(new AccountResponse(account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> authenticate(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response,
            AccountUser account) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException exception) {
            throw new AuthenticationServiceException("New account could not be authenticated", exception);
        }
        return saveAuthentication(authentication, request, response, account);
    }

    private ResponseEntity<?> saveAuthentication(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            AccountUser account) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        } else {
            request.getSession(true);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return ResponseEntity.ok(new AccountResponse(account));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public record MessageResponse(String message) { }
}
