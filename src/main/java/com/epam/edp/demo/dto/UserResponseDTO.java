package com.epam.edp.demo.dto;


import java.time.Instant;

import com.epam.edp.demo.enums.Role;
import com.epam.edp.demo.model.User;

public record UserResponseDTO(
        String id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Instant createdAt
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
