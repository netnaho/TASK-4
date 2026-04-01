package com.pharmaprocure.portal.security;

import com.pharmaprocure.portal.enums.DataScope;

public record PermissionGrant(
    Permission permission,
    DataScope dataScope
) {
}
