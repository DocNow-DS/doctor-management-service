package com.healthcare.doctor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctors")
public class Doctor implements UserDetails {

    @Id
    private String id;

    // Link to the shared users collection (auth)
    private String userId;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private String hospitalName;
    private Integer yearsOfExperience;
    private String education;
    private String about;
    private String profileImageUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private Set<Role> roles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- UserDetails implementation ----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null)
            return List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();
    }

    /** Password is not stored in this service; return empty string. */
    @Override
    public String getPassword() {
        return "";
    }

    /** Username for Spring Security = email. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
    }
}
