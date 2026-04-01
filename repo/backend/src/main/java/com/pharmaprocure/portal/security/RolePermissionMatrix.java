package com.pharmaprocure.portal.security;

import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionMatrix {

    private final Map<RoleName, List<PermissionGrant>> permissionMap = new EnumMap<>(RoleName.class);
    private final Map<RoleName, Map<Permission, DataScope>> permissionScopeMap = new EnumMap<>(RoleName.class);

    public RolePermissionMatrix() {
        registerRole(RoleName.BUYER, List.of(
            new PermissionGrant(Permission.DASHBOARD_VIEW, DataScope.SELF),
            new PermissionGrant(Permission.ORDER_VIEW, DataScope.SELF),
            new PermissionGrant(Permission.ORDER_CREATE, DataScope.SELF),
            new PermissionGrant(Permission.ORDER_RECEIVE, DataScope.SELF),
            new PermissionGrant(Permission.ORDER_RETURN, DataScope.SELF),
            new PermissionGrant(Permission.ORDER_EXCEPTION_CREATE, DataScope.SELF),
            new PermissionGrant(Permission.DOCUMENT_VIEW, DataScope.SELF),
            new PermissionGrant(Permission.DOCUMENT_CREATE, DataScope.SELF),
            new PermissionGrant(Permission.DOCUMENT_EDIT, DataScope.SELF),
            new PermissionGrant(Permission.DOCUMENT_DOWNLOAD, DataScope.SELF),
            new PermissionGrant(Permission.CHECKIN_CREATE, DataScope.SELF),
            new PermissionGrant(Permission.CHECKIN_VIEW, DataScope.SELF),
            new PermissionGrant(Permission.CHECKIN_EDIT, DataScope.SELF),
            new PermissionGrant(Permission.CHECKIN_DOWNLOAD, DataScope.SELF),
            new PermissionGrant(Permission.CRITICAL_ACTION_REQUEST, DataScope.SELF),
            new PermissionGrant(Permission.CRITICAL_ACTION_VIEW, DataScope.SELF),
            new PermissionGrant(Permission.USER_VIEW_SELF, DataScope.SELF)
        ));
        registerRole(RoleName.FULFILLMENT_CLERK, List.of(
            new PermissionGrant(Permission.DASHBOARD_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.ORDER_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.ORDER_FULFILL, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.DOCUMENT_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.USER_VIEW_SELF, DataScope.SELF)
        ));
        registerRole(RoleName.QUALITY_REVIEWER, List.of(
            new PermissionGrant(Permission.DASHBOARD_VIEW, DataScope.TEAM),
            new PermissionGrant(Permission.ORDER_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_REVIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_DOWNLOAD, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.CRITICAL_ACTION_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.CRITICAL_ACTION_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.APPROVAL_REVIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.USER_VIEW_SELF, DataScope.SELF)
        ));
        registerRole(RoleName.FINANCE, List.of(
            new PermissionGrant(Permission.DASHBOARD_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.ORDER_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.ORDER_PAYMENT_RECORD, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.CRITICAL_ACTION_VIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.CRITICAL_ACTION_APPROVE, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.APPROVAL_REVIEW, DataScope.ORGANIZATION),
            new PermissionGrant(Permission.USER_VIEW_SELF, DataScope.SELF)
        ));
        registerRole(RoleName.SYSTEM_ADMINISTRATOR, List.of(
            new PermissionGrant(Permission.DASHBOARD_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_CREATE, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_REVIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_PAYMENT_RECORD, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_FULFILL, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_RECEIVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_RETURN, DataScope.GLOBAL),
            new PermissionGrant(Permission.ORDER_EXCEPTION_CREATE, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_CREATE, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_EDIT, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_ARCHIVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_DOWNLOAD, DataScope.GLOBAL),
            new PermissionGrant(Permission.DOCUMENT_TEMPLATE_MANAGE, DataScope.GLOBAL),
            new PermissionGrant(Permission.CHECKIN_CREATE, DataScope.GLOBAL),
            new PermissionGrant(Permission.CHECKIN_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.CHECKIN_EDIT, DataScope.GLOBAL),
            new PermissionGrant(Permission.CHECKIN_DOWNLOAD, DataScope.GLOBAL),
            new PermissionGrant(Permission.CRITICAL_ACTION_REQUEST, DataScope.GLOBAL),
            new PermissionGrant(Permission.CRITICAL_ACTION_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.CRITICAL_ACTION_APPROVE, DataScope.GLOBAL),
            new PermissionGrant(Permission.ADMIN_CONFIG_VIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.REASON_CODE_MANAGE, DataScope.GLOBAL),
            new PermissionGrant(Permission.APPROVAL_REVIEW, DataScope.GLOBAL),
            new PermissionGrant(Permission.ADMIN_ACCESS, DataScope.GLOBAL),
            new PermissionGrant(Permission.USER_VIEW_SELF, DataScope.GLOBAL)
        ));
    }

    private void registerRole(RoleName roleName, List<PermissionGrant> grants) {
        Set<Permission> seen = new HashSet<>();
        Map<Permission, DataScope> scopes = new EnumMap<>(Permission.class);
        for (PermissionGrant grant : grants) {
            if (!seen.add(grant.permission())) {
                throw new IllegalStateException("Duplicate permission mapping for role " + roleName + ": " + grant.permission().name());
            }
            scopes.put(grant.permission(), grant.dataScope());
        }
        permissionMap.put(roleName, List.copyOf(grants));
        permissionScopeMap.put(roleName, Map.copyOf(scopes));
    }

    public List<PermissionGrant> getGrants(RoleName roleName) {
        return permissionMap.getOrDefault(roleName, List.of());
    }

    public Optional<DataScope> getDataScope(RoleName roleName, Permission permission) {
        return Optional.ofNullable(permissionScopeMap.getOrDefault(roleName, Map.of()).get(permission));
    }

    public Set<Permission> getPermissions(RoleName roleName) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        getGrants(roleName).forEach(grant -> permissions.add(grant.permission()));
        return permissions;
    }
}
