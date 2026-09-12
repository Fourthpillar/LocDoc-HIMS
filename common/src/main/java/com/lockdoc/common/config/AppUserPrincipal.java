package com.lockdoc.common.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * The principal every request carries, holding the two facts services keep
 * asking for: which user, and which facility they act within.
 *
 * Spring's default {@code User} principal has no room for either, so anything
 * needing the acting user's id had to re-query the user table by username on
 * every call. Read them off {@code @AuthenticationPrincipal AppUserPrincipal}
 * instead, or through {@link SecurityUtils}.
 *
 * {@code facilityId} is null for a user who belongs to no facility — Super
 * Admin, who works across all of them, and a doctor, whose identity is central
 * and whose facilities are a mapping rather than a column.
 */
public class AppUserPrincipal extends User {

    private final Long userId;
    private final Long facilityId;

    public AppUserPrincipal(String username, String password, boolean enabled, boolean accountNonLocked,
                            Collection<? extends GrantedAuthority> authorities, Long userId, Long facilityId) {
        super(username, password, enabled, true, true, accountNonLocked, authorities);
        this.userId = userId;
        this.facilityId = facilityId;
    }

    public Long getUserId() {
        return userId;
    }

    /** Null when the user belongs to no single facility — see the class javadoc. */
    public Long getFacilityId() {
        return facilityId;
    }

    /** Checks the authority, not facilityId-nullness: a doctor has no facility either. */
    public boolean isSuperAdmin() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    public boolean isDoctor() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
    }
}
