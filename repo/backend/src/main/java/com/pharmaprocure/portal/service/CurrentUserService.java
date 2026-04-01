package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.repository.UserRepository;
import com.pharmaprocure.portal.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findWithRoleByUsername(principal.username())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
