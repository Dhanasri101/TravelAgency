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
import com.epam.edp.demo.dto.ApiErrorResponseDTO;
import com.epam.edp.demo.dto.PasswordResetRequestDTO;
import com.epam.edp.demo.dto.VerifyResetCodeDTO;
import com.epam.edp.demo.dto.ResetPasswordDTO;
import com.epam.edp.demo.exception.UnauthenticatedException;
import com.epam.edp.demo.model.User;
import com.epam.edp.demo.service.UserService;
import com.epam.edp.demo.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and current user endpoints")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/sign-up")
        @Operation(summary = "Register a new user", description = "Creates a user account with email/password credentials.")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = SignUpResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or weak password",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
        })
            public ResponseEntity<SignUpResponseDTO> signUp(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details for the new user account.",
                    required = true)
                @Valid @RequestBody SignUpRequestDTO request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponseDTO.ok());
    }

    @PostMapping("/sign-in")
        @Operation(summary = "Sign in", description = "Authenticates a user and returns an access token.")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed in successfully",
                content = @Content(schema = @Schema(implementation = SignInResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
            @ApiResponse(responseCode = "423", description = "Account temporarily locked",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
        })
            public ResponseEntity<SignInResponseDTO> signIn(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email/password credentials used to obtain a JWT token.",
                    required = true)
                @Valid @RequestBody SignInRequestDTO request) {
        return ResponseEntity.ok(userService.signIn(request));
    }

    @GetMapping("/me")
        @Operation(summary = "Get current user", description = "Returns profile details of the authenticated user.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user details",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
        })
    public ResponseEntity<UserResponseDTO> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new UnauthenticatedException("Missing or invalid token");
        }
        String userId = auth.getName();
        User user = userService.requireById(userId);
        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, you will receive a verification code shortly"));
    }

    @PostMapping("/password-reset/verify")
    public ResponseEntity<Map<String, String>> verifyResetCode(@Valid @RequestBody VerifyResetCodeDTO request) {
        passwordResetService.verifyResetCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "Verification code is valid"));
    }

    @PostMapping("/password-reset/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
    }
}

