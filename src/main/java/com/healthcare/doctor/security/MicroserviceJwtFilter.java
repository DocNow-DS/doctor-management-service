package com.healthcare.doctor.security;

import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.service.PatientServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MicroserviceJwtFilter extends OncePerRequestFilter {

    private final PatientServiceClient patientServiceClient;

    public MicroserviceJwtFilter(PatientServiceClient patientServiceClient) {
        this.patientServiceClient = patientServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        final String requestPath = request.getRequestURI();
        
        log.debug("Processing request: {} with Authorization header present: {}", 
                requestPath, authorizationHeader != null);
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            log.debug("Token extracted, calling patient service to validate");

            try {
                AuthResponse authResponse = patientServiceClient.validateToken(jwt);
                log.debug("Patient service validation response: {}", authResponse);
                
                if (authResponse != null && authResponse.getUser() != null) {
                    AuthResponse.User user = authResponse.getUser();
                    log.debug("User from auth response: id={}, username={}, roles={}", 
                            user.getId(), user.getUsername(), Arrays.toString(user.getRoles()));
                    
                    if (user.getRoles() == null || user.getRoles().length == 0) {
                        log.warn("User has no roles! Authentication will fail for @PreAuthorize checks");
                    }
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null,
                            Arrays.stream(user.getRoles())
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList())
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set in SecurityContext for user: {}", user.getUsername());
                } else {
                    log.warn("Auth response or user is null");
                }
            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage(), e);
            }
        } else {
            log.debug("No Authorization header or doesn't start with Bearer");
        }
        
        filterChain.doFilter(request, response);
    }
}
