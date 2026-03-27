package com.healthcare.doctor.security;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.service.PatientServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PatientServiceClient patientServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // This method is not used directly - authentication is done via JWT token validation with patient service
        // The JwtAuthenticationFilter handles token validation by calling patient service
        throw new UsernameNotFoundException("Use JWT token authentication via patient service");
    }

    /**
     * Build UserDetails from AuthResponse returned by patient service.
     * Used by JwtAuthenticationFilter after validating token with patient service.
     */
    public UserDetails buildUserDetailsFromAuthResponse(AuthResponse authResponse) {
        if (authResponse == null || authResponse.getUser() == null) {
            throw new UsernameNotFoundException("Invalid authentication response");
        }

        AuthResponse.User user = authResponse.getUser();

        List<GrantedAuthority> authorities = Arrays.stream(user.getRoles())
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());

        return User.builder()
                .username(user.getId()) // Use user ID as principal identifier
                .password("") // Password is not stored here
                .authorities(authorities)
                .build();
    }
}
