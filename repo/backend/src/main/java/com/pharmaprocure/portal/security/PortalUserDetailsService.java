package com.pharmaprocure.portal.security;

import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.repository.UserRepository;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionMatrix rolePermissionMatrix;

    public PortalUserDetailsService(UserRepository userRepository, RolePermissionMatrix rolePermissionMatrix) {
        this.userRepository = userRepository;
        this.rolePermissionMatrix = rolePermissionMatrix;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findWithRoleByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getOrganizationCode(),
            user.getPasswordHash(),
            user.getRole().getName(),
            Set.copyOf(rolePermissionMatrix.getGrants(user.getRole().getName())),
            user.isActive()
        );
    }
}
