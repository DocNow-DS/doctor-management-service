package com.healthcare.doctor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.status(302).body(Map.of(
                "message", "Please use patient-management-service for authentication",
                "loginUrl", "http://localhost:8081/api/auth/login"
        ));
    }
}
