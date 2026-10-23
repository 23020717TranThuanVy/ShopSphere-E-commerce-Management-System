package com.shopsphere.repository;

import com.shopsphere.entity.AccountUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
    Optional<AccountUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
