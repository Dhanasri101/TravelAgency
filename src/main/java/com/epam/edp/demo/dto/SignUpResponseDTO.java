package com.epam.edp.demo.dto;

public record SignUpResponseDTO(String message) {
    public static SignUpResponseDTO ok() {
        return new SignUpResponseDTO("Account created successfully");
    }
}

