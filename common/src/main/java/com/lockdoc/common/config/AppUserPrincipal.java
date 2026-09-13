package com.lockdoc.common.config;

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
 * {@code currentUserId(UserDetails)} helper duplicated across
 * controllers). That indirection is gone: read userId/facilityId
 * straight off {@code @AuthenticationPrincipal AppUserPrincipal}.
 *
 * facilityId is {@code null} only for Super Admin, who is the one role
 * scoped across every facility rather than to one (Master Spec §4).
 *
 * <b>Acting context (Super Admin only).</b> Super Admin can work inside any
 * module the way that module's own users do, by picking a facility (OP and
 * Hospital/Clinic Admin screens) or a doctor (Doctor screens) in the UI's
 * module switcher. JwtAuthenticationFilter copies those choices from the
 * {@code X-Acting-Facility-Id}/{@code X-Acting-Doctor-Id} request headers
 * onto the principal via {@link #withActingContext}, and only for a Super
 * Admin - any other user's headers are ignored, so a facility user can never
 * step outside their own facility.
 */
public class AppUserPrincipal extends User {

    public static final String ACTING_FACILITY_HEADER = "X-Acting-Facility-Id";
    public static final String ACTING_DOCTOR_HEADER = "X-Acting-Doctor-Id";

    private final Long userId;
    private final Long facilityId;
    private final Long actingFacilityId;
    private final Long actingDoctorId;

    public AppUserPrincipal(String username, String password, boolean enabled, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities, Long userId, Long facilityId) {
        this(username, password, enabled, accountNonLocked, authorities, userId, facilityId, null, null);
    }

    private AppUserPrincipal(String username, String password, boolean enabled, boolean accountNonLocked,
                              Collection<? extends GrantedAuthority> authorities, Long userId, Long facilityId,
                              Long actingFacilityId, Long actingDoctorId) {
        super(username, password, enabled, true, true, accountNonLocked, authorities);
        this.userId = userId;
        this.facilityId = facilityId;
        this.actingFacilityId = actingFacilityId;
        this.actingDoctorId = actingDoctorId;
    }

    /** A copy of this principal acting at the given facility/as the given doctor. Super Admin only - see class javadoc. */
    public AppUserPrincipal withActingContext(Long actingFacilityId, Long actingDoctorId) {
        if (!isSuperAdmin()) {
            throw new IllegalStateException("Only Super Admin can act on behalf of a facility or doctor");
        }
        return new AppUserPrincipal(getUsername(), getPassword(), isEnabled(), isAccountNonLocked(), getAuthorities(),
                userId, facilityId, actingFacilityId, actingDoctorId);
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

    /** The facility Super Admin picked in the module switcher, or {@code null}. Always {@code null} for other users. */
    public Long getActingFacilityId() {
        return actingFacilityId;
    }

    /** The doctor Super Admin picked in the module switcher, or {@code null}. Always {@code null} for other users. */
    public Long getActingDoctorId() {
        return actingDoctorId;
    }

    /** Checks the actual authority, not facilityId-nullness - see {@link #getFacilityId()}. */
    public boolean isSuperAdmin() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    /** A real Doctor, or Super Admin acting as one - either way, doctor self-service rules apply. */
    public boolean isDoctor() {
        return getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()))
                || (actingDoctorId != null && isSuperAdmin());
    }
}
