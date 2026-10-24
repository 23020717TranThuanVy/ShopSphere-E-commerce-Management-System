package com.shopsphere.dto;

import com.shopsphere.entity.AccountRole;
import com.shopsphere.entity.AccountStatus;
import com.shopsphere.entity.AccountUser;
import java.time.LocalDateTime;

public class AccountResponse {
    private final Long id;
    private final String displayName;
    private final String email;
    private final AccountRole role;
    private final AccountStatus status;
    private final String storeName;
    private final LocalDateTime createdAt;

    public AccountResponse(AccountUser user) {
        this.id = user.getId();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.storeName = user.getStoreName();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public AccountRole getRole() { return role; }
    public AccountStatus getStatus() { return status; }
    public String getStoreName() { return storeName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
