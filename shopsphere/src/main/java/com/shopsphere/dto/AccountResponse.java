package com.shopsphere.dto;

import com.shopsphere.entity.AccountRole;
import com.shopsphere.entity.AccountUser;

public class AccountResponse {
    private final Long id;
    private final String displayName;
    private final String email;
    private final AccountRole role;

    public AccountResponse(AccountUser user) {
        this.id = user.getId();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public AccountRole getRole() { return role; }
}
