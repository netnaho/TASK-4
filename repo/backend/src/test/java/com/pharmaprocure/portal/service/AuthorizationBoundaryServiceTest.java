package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import com.pharmaprocure.portal.security.RolePermissionMatrix;
import org.junit.jupiter.api.Test;

class AuthorizationBoundaryServiceTest {

    private final PermissionAuthorizationService permissionAuthorizationService = new PermissionAuthorizationService(new RolePermissionMatrix());

    @Test
    void organizationScopedFinanceCannotAccessDifferentOrganizationOrder() {
        UserEntity finance = user(10L, RoleName.FINANCE, "ORG-ALPHA");

        boolean allowed = permissionAuthorizationService.canAccessResource(
            finance,
            Permission.ORDER_VIEW,
            99L,
            RoleName.BUYER,
            "ORG-BETA"
        );

        assertFalse(allowed);
    }

    @Test
    void organizationScopedFulfillmentCannotAccessDifferentOrganizationDocument() {
        UserEntity fulfillment = user(11L, RoleName.FULFILLMENT_CLERK, "ORG-ALPHA");

        boolean allowed = permissionAuthorizationService.canAccessResource(
            fulfillment,
            Permission.DOCUMENT_VIEW,
            99L,
            RoleName.BUYER,
            "ORG-BETA"
        );

        assertFalse(allowed);
    }

    @Test
    void selfScopedBuyerCannotAccessAnotherUsersCheckin() {
        UserEntity buyer = user(12L, RoleName.BUYER, "ORG-ALPHA");

        boolean allowed = permissionAuthorizationService.canAccessResource(
            buyer,
            Permission.CHECKIN_VIEW,
            13L,
            RoleName.BUYER,
            "ORG-ALPHA"
        );

        assertFalse(allowed);
    }

    @Test
    void globalScopedUserCanAccessGlobalOwnedResource() {
        UserEntity quality = user(14L, RoleName.QUALITY_REVIEWER, "ORG-ALPHA");

        boolean allowed = permissionAuthorizationService.canAccessResource(
            quality,
            Permission.ORDER_VIEW,
            99L,
            RoleName.SYSTEM_ADMINISTRATOR,
            "PLATFORM"
        );

        assertTrue(allowed);
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
}
