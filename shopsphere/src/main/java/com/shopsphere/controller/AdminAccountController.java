package com.shopsphere.controller;

import com.shopsphere.dto.AccountResponse;
import com.shopsphere.entity.AccountUser;
import com.shopsphere.repository.AccountUserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AccountUserRepository accountUserRepository;

    public AdminAccountController(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    @GetMapping
    public List<AccountResponse> listAccounts() {
        return accountUserRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(AccountResponse::new)
                .toList();
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveSeller(@PathVariable Long id) {
        return updateSellerApplication(id, true);
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectSeller(@PathVariable Long id) {
        return updateSellerApplication(id, false);
    }

    private ResponseEntity<?> updateSellerApplication(Long id, boolean approve) {
        AccountUser account = accountUserRepository.findById(id).orElse(null);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthController.MessageResponse("Account not found"));
        }
        try {
            if (approve) {
                account.approveSeller();
            } else {
                account.rejectSeller();
            }
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(new AuthController.MessageResponse(exception.getMessage()));
        }
        accountUserRepository.save(account);
        return ResponseEntity.ok(new AccountResponse(account));
    }
}
