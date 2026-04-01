package com.pharmaprocure.portal.security;

import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permissionAuth")
public class PermissionAuthorizationService {

    private final RolePermissionMatrix rolePermissionMatrix;

    public PermissionAuthorizationService(RolePermissionMatrix rolePermissionMatrix) {
        this.rolePermissionMatrix = rolePermissionMatrix;
    }

    public boolean hasPermission(Authentication authentication, String permissionName) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Permission permission;
        try {
            permission = Permission.valueOf(permissionName);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.hasPermission(permission);
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(permission.name()));
    }

    public DataScope requireDataScope(UserEntity actor, Permission permission) {
        RoleName roleName = actor.getRole().getName();
        return rolePermissionMatrix.getDataScope(roleName, permission)
            .orElseThrow(() -> new IllegalStateException("No data scope grant for role " + roleName + " and permission " + permission.name()));
    }

    public boolean canAccessResource(UserEntity actor, Permission permission, Long ownerUserId, RoleName ownerRoleName, String ownerOrganizationCode) {
        DataScope scope = requireDataScope(actor, permission);
        if (scope == DataScope.GLOBAL) {
            return true;
        }
        if (ownerUserId != null && ownerUserId.equals(actor.getId())) {
            return true;
        }
        if (scope == DataScope.SELF) {
            return false;
        }
        if (scope == DataScope.ORGANIZATION) {
            return ownerOrganizationCode != null && ownerOrganizationCode.equals(actor.getOrganizationCode());
        }
        if (ownerRoleName == null) {
            return false;
        }
        return ownerOrganizationCode != null
            && ownerOrganizationCode.equals(actor.getOrganizationCode())
            && ownerRoleName == actor.getRole().getName();
    }

    public <T> List<T> filterByScope(
        UserEntity actor,
        Permission permission,
        List<T> resources,
        Function<T, Long> ownerUserIdExtractor,
        Function<T, RoleName> ownerRoleExtractor,
        Function<T, String> ownerOrganizationExtractor
    ) {
        return resources.stream()
            .filter(resource -> canAccessResource(
                actor,
                permission,
                ownerUserIdExtractor.apply(resource),
                ownerRoleExtractor.apply(resource),
                ownerOrganizationExtractor.apply(resource)
            ))
            .toList();
    }
}
