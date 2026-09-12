package com.lockdoc.common.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads the acting user and facility off the current request's
 * {@link AppUserPrincipal}.
 *
 * Facility-scoped services call {@link #requireFacilityId()} rather than
 * trusting a facilityId sent by the client — a tenant boundary that depends on
 * a request parameter is not a boundary.
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

    /** Null for a user who belongs to no facility — scoped callers want {@link #requireFacilityId()}. */
    public static Long currentFacilityId() {
        return currentPrincipal().getFacilityId();
    }

    /**
     * For every facility-scoped operation. Throws rather than returning null, so
     * a scoped service can never quietly run an unscoped query because the caller
     * happened to be a user without a facility.
     */
    /**
     * The facility a scoped read or write should be confined to, or {@code null}
     * meaning "every facility".
     *
     * This is the cross-facility counterpart to {@link #requireFacilityId()}, and the
     * difference between them is a judgement about the caller, not about the data: a
     * user who belongs to one facility may only ever see that one, while Super Admin
     * belongs to none precisely because the platform is theirs to see whole. Treating
     * their missing facility as an error made every facility-scoped screen unusable to
     * the one role meant to oversee all of them.
     *
     * Callers must handle the null: a list widens to every facility, and an action on
     * a specific record skips the "is this mine" check because the record carries its
     * own facility. A caller that cannot express "all" — creating something that must
     * land in one facility — asks for the facility explicitly instead.
     *
     * Anyone else without a facility is still refused; only Super Admin gets the
     * widened scope.
     */
    public static Long facilityScopeOrAll() {
        AppUserPrincipal principal = currentPrincipal();
        if (principal.getFacilityId() != null) {
            return principal.getFacilityId();
        }
        if (principal.isSuperAdmin()) {
            return null;
        }
        throw new IllegalStateException(
                "This operation requires a facility-scoped user; the current principal has no facility.");
    }

    public static Long requireFacilityId() {
        Long facilityId = currentFacilityId();
        if (facilityId == null) {
            throw new IllegalStateException(
                    "This operation requires a facility-scoped user; the current principal has no facility. "
                            + "Super Admin acts through platform-level endpoints, not facility-scoped ones.");
        }
        return facilityId;
    }
}
