package com.epam.edp.demo.dto;

import java.time.Instant;

import com.epam.edp.demo.enums.Role;

public record SignInResponseDTO(
        String idToken,
        Role role,
        String userName,
        String email,
        Instant expiresAt
) {}

