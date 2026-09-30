package com.ecommerce.users.controller;

import com.ecommerce.users.dto.ChangePasswordRequest;
import com.ecommerce.users.dto.SellerApplicationRequest;
import com.ecommerce.users.dto.SellerApplicationResponse;
import com.ecommerce.users.dto.UpdateProfileRequest;
import com.ecommerce.users.dto.UserResponse;
import com.ecommerce.users.service.UserService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return UserResponse.from(userService.getById(userId));
    }

    @PutMapping
    public UserResponse update(@AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        return UserResponse.from(userService.updateProfile(userId, request.fullName()));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.currentPassword(), request.newPassword());
    }

    @PostMapping("/seller-application")
    public SellerApplicationResponse apply(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody SellerApplicationRequest request) {
        return SellerApplicationResponse.from(userService.applyToBeSeller(userId, request.businessName()));
    }

    @GetMapping("/seller-applications/pending")
    public List<SellerApplicationResponse> pendingApplications() {
        return userService.getPendingApplications().stream().map(SellerApplicationResponse::from).toList();
    }

    @PutMapping("/seller-applications/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveApplication(@PathVariable Long id) {
        userService.decideApplication(id, true);
    }

    @PutMapping("/seller-applications/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectApplication(@PathVariable Long id) {
        userService.decideApplication(id, false);
    }
}