package com.ecommerce.users.service;

import com.ecommerce.common.NotFoundException;
import com.ecommerce.users.model.ApplicationStatus;
import com.ecommerce.users.model.Role;
import com.ecommerce.users.model.SellerApplication;
import com.ecommerce.users.model.User;
import com.ecommerce.users.repository.SellerApplicationRepository;
import com.ecommerce.users.repository.UserRepository;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerApplicationRepository applicationRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SellerApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationRepository = applicationRepository;

    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User updateProfile(Long userId, String fullName) {
        User user = getById(userId);
        user.setFullName(fullName.trim());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalStateException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public SellerApplication applyToBeSeller(Long userId, String businessName) {
        User user = getById(userId);
        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalStateException("Only customer accounts can apply to become a seller");
        }
        if (applicationRepository.findByUserIdAndStatus(userId, ApplicationStatus.PENDING).isPresent()) {
            throw new IllegalStateException("You already have a pending seller application");
        }
        SellerApplication application = new SellerApplication();
        application.setUserId(userId);
        application.setBusinessName(businessName);
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<SellerApplication> getPendingApplications() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    @Transactional
    public void decideApplication(Long applicationId, boolean approve) {
        SellerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        application.setStatus(approve ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        if (approve) {
            User user = getById(application.getUserId());
            user.setRole(Role.SELLER);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateDefaultAddress(Long userId, String phone, String city, String addressLine) {
        User user = getById(userId);
        user.setDefaultPhone(phone);
        user.setDefaultCity(city);
        user.setDefaultAddressLine(addressLine);
        userRepository.save(user);
    }
}