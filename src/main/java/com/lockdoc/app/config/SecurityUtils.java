package com.lockdoc.app.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads the current request's facility/user context off the
 * AppUserPrincipal set by JwtAuthenticationFilter. Every service method
 * that must not leak data across tenants calls {@link #requireFacilityId()}
 * rather than trusting a client-supplied facilityId - see Master Spec §5
 * principle 1 and §14.1 (the retrofit this class exists to make possible).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AppUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated AppUserPrincipal in the current security context");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentPrincipal().getUserId();
    }

    /** {@code null} for Super Admin - callers scoped to one facility should use {@link #requireFacilityId()} instead. */
    public static Long currentFacilityId() {
        return currentPrincipal().getFacilityId();
    }

    /**
     * For every facility-scoped operation (i.e. everything except Super
     * Admin's cross-facility screens). Throws rather than silently
     * returning null, so a facility-scoped service can never accidentally
     * run an unscoped query because the caller happened to be Super Admin.
     */
    public static Long requireFacilityId() {
        Long facilityId = currentFacilityId();
        if (facilityId == null) {
            throw new IllegalStateException(
                    "This operation requires a facility-scoped user; the current principal (Super Admin) has none. "
                            + "Super Admin acts through platform-level endpoints, not facility-scoped ones.");
        }
        return facilityId;
    }
}
