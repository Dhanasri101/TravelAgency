package com.epam.edp.demo.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.edp.demo.dto.SignInRequestDTO;
import com.epam.edp.demo.dto.SignInResponseDTO;
import com.epam.edp.demo.dto.SignUpRequestDTO;
import com.epam.edp.demo.dto.SignUpResponseDTO;
import com.epam.edp.demo.dto.UserResponseDTO;
import com.epam.edp.demo.exception.UnauthenticatedException;
import com.epam.edp.demo.model.User;
import com.epam.edp.demo.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponseDTO.ok());
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponseDTO> signIn(@Valid @RequestBody SignInRequestDTO request) {
        return ResponseEntity.ok(userService.signIn(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new UnauthenticatedException("Missing or invalid token");
        }
        String userId = auth.getName();
        User user = userService.requireById(userId);
        return ResponseEntity.ok(UserResponseDTO.from(user));
    }
}

