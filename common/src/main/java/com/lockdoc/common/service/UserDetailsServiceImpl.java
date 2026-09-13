package com.lockdoc.common.service;

import com.lockdoc.common.config.AppUserPrincipal;
import com.lockdoc.common.entity.Right;
import com.lockdoc.common.entity.Role;
import com.lockdoc.common.entity.User;
import com.lockdoc.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
            for (Right right : role.getRights()) {
                authorities.add(new SimpleGrantedAuthority(right.getRightCode()));
            }
        }

        // AppUserPrincipal (not Spring's default User) so every downstream
        // request carries userId + facilityId without re-querying the DB -
        // see SecurityUtils, which is how every facility-scoped service
        // gets its tenant boundary.
        return new AppUserPrincipal(
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                Boolean.TRUE.equals(user.getAccountNonLocked()),
                authorities,
                user.getId(),
                user.getFacility() != null ? user.getFacility().getId() : null
        );
    }
}
