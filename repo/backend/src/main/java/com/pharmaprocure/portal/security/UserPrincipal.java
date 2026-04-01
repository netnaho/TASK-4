package com.pharmaprocure.portal.security;

import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(
    Long id,
    String username,
    String displayName,
    String organizationCode,
    String password,
    RoleName role,
    Set<PermissionGrant> grants,
    boolean active
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        grants.forEach(grant -> {
            authorities.add(new SimpleGrantedAuthority(grant.permission().name()));
            authorities.add(new SimpleGrantedAuthority(grant.permission().name() + ":" + grant.dataScope().name()));
        });
        return authorities;
    }

    public boolean hasPermission(Permission permission) {
        return grants.stream().anyMatch(grant -> grant.permission() == permission);
    }

    public DataScope dataScopeFor(Permission permission) {
        return grants.stream()
            .filter(grant -> grant.permission() == permission)
            .map(PermissionGrant::dataScope)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
