package com.shopsphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
public class AccountUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String displayName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(length = 120)
    private String storeName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected AccountUser() {
    }

    public AccountUser(String displayName, String email, String passwordHash, AccountRole role) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = AccountStatus.ACTIVE;
    }

    public AccountUser(String displayName, String email, String passwordHash, AccountRole role,
            AccountStatus status, String storeName, boolean enabled) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.storeName = storeName;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public AccountRole getRole() { return role; }
    public AccountStatus getStatus() { return status == null ? AccountStatus.ACTIVE : status; }
    public String getStoreName() { return storeName; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void approveSeller() {
        if (role != AccountRole.SELLER || getStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException("Only pending Seller accounts can be approved");
        }
        status = AccountStatus.ACTIVE;
        enabled = true;
    }

    public void rejectSeller() {
        if (role != AccountRole.SELLER || getStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException("Only pending Seller accounts can be rejected");
        }
        status = AccountStatus.REJECTED;
        enabled = false;
    }
}
