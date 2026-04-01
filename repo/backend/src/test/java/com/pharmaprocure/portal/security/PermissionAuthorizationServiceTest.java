package com.pharmaprocure.portal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class PermissionAuthorizationServiceTest {

    private final RolePermissionMatrix rolePermissionMatrix = new RolePermissionMatrix();
    private final PermissionAuthorizationService service = new PermissionAuthorizationService(rolePermissionMatrix);

    @Test
    void resolvesGrantedPermissionFromPrincipal() {
        UserPrincipal principal = principal(RoleName.BUYER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertTrue(service.hasPermission(authentication, "ORDER_VIEW"));
        assertFalse(service.hasPermission(authentication, "ADMIN_ACCESS"));
        assertFalse(service.hasPermission(authentication, "UNKNOWN_PERMISSION"));
    }

    @Test
    void enforcesSelfScopeBoundaries() {
        UserEntity buyer = user(100L, RoleName.BUYER, "ORG-ALPHA");

        assertEquals(DataScope.SELF, service.requireDataScope(buyer, Permission.ORDER_VIEW));
        assertTrue(service.canAccessResource(buyer, Permission.ORDER_VIEW, 100L, RoleName.BUYER, "ORG-ALPHA"));
        assertFalse(service.canAccessResource(buyer, Permission.ORDER_VIEW, 101L, RoleName.BUYER, "ORG-ALPHA"));
    }

    @Test
    void enforcesOrganizationScopeBoundaries() {
        UserEntity finance = user(200L, RoleName.FINANCE, "ORG-ALPHA");

        assertEquals(DataScope.ORGANIZATION, service.requireDataScope(finance, Permission.ORDER_VIEW));
        assertTrue(service.canAccessResource(finance, Permission.ORDER_VIEW, 201L, RoleName.FINANCE, "ORG-ALPHA"));
        assertTrue(service.canAccessResource(finance, Permission.ORDER_VIEW, 101L, RoleName.BUYER, "ORG-ALPHA"));
        assertFalse(service.canAccessResource(finance, Permission.ORDER_VIEW, 202L, RoleName.BUYER, "ORG-BETA"));
        assertFalse(service.canAccessResource(finance, Permission.ORDER_VIEW, 301L, RoleName.SYSTEM_ADMINISTRATOR, "PLATFORM"));
    }

    @Test
    void allowsGlobalScopeAcrossTeams() {
        UserEntity admin = user(300L, RoleName.SYSTEM_ADMINISTRATOR, "PLATFORM");

        assertEquals(DataScope.GLOBAL, service.requireDataScope(admin, Permission.ORDER_VIEW));
        assertTrue(service.canAccessResource(admin, Permission.ORDER_VIEW, 101L, RoleName.BUYER, "ORG-ALPHA"));
        assertTrue(service.canAccessResource(admin, Permission.ORDER_VIEW, 201L, RoleName.FINANCE, "ORG-BETA"));
    }

    private UserEntity user(Long id, RoleName roleName, String organizationCode) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRole(role);
        user.setUsername(roleName.name().toLowerCase());
        user.setDisplayName(roleName.name());
        user.setOrganizationCode(organizationCode);
        return user;
    }

    private UserPrincipal principal(RoleName roleName) {
        return new UserPrincipal(
            1L,
            roleName.name().toLowerCase(),
            roleName.name(),
            "ORG-ALPHA",
            "hash",
            roleName,
            Set.copyOf(rolePermissionMatrix.getGrants(roleName)),
            true
        );
    }
}
