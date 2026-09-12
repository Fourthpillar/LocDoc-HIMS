package com.lockdoc.app.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom Spring Security principal carrying the two facts almost every
 * service in this application needs on every request: which user, and
 * which facility. Replaces Spring's default {@code User} principal
 * (previously returned by UserDetailsServiceImpl), which had no room for
 * facilityId - every controller that needed the acting user's id had to
 * re-query UserRepository by username to get it (see e.g. the old
 * {@code currentUserId(UserDetails)} helper duplicated across the
 * pharmacy controllers). That indirection is gone: read userId/facilityId
 * straight off {@code @AuthenticationPrincipal AppUserPrincipal}.
 *
 * facilityId is {@code null} only for Super Admin, who is the one role
 * scoped across every facility rather than to one (Master Spec §4).
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

    /**
     * {@code null} for Super Admin AND for Doctor - see class javadoc.
     * {@code facilityId == null} alone does NOT mean Super Admin; use
     * {@link #isSuperAdmin()} to actually distinguish the two.
     */
    public Long getFacilityId() {
        return facilityId;
    }

    /** Checks the actual authority, not facilityId-nullness - see {@link #getFacilityId()}. */
    public boolean isSuperAdmin() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    public boolean isDoctor() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
    }
}
