package com.epam.edp.demo.service;


import com.epam.edp.demo.dto.SignInRequestDTO;
import com.epam.edp.demo.dto.SignInResponseDTO;
import com.epam.edp.demo.dto.SignUpRequestDTO;
import com.epam.edp.demo.enums.Role;
import com.epam.edp.demo.exception.AccountLockedException;
import com.epam.edp.demo.exception.EmailAlreadyExistsException;
import com.epam.edp.demo.exception.InvalidCredentialsException;
import com.epam.edp.demo.exception.WeakPasswordException;
import com.epam.edp.demo.model.User;
import com.epam.edp.demo.repository.UserRepository;
import com.epam.edp.demo.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int maxFailedAttempts;
    private final Duration lockoutDuration;

    public UserService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.auth.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${app.auth.lockout-minutes:15}") long lockoutMinutes
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    public User signUp(SignUpRequestDTO req) {
        String firstName = req.getFirstName().trim();
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = req.getPassword();

        rejectPasswordContainingIdentity(password, firstName, email);

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String lastName = req.getLastName().trim();
        User user = new User(firstName, lastName, email, passwordEncoder.encode(password), Role.CUSTOMER);
        try {
            return repository.save(user);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new EmailAlreadyExistsException(email, e);
        }
    }

    public SignInResponseDTO signIn(SignInRequestDTO req) {
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = req.getPassword();

        User user = repository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            long retryAfter = Duration.between(now, user.getLockedUntil()).toSeconds();
            throw new AccountLockedException(Math.max(retryAfter, 1));
        }
        if (user.getLockedUntil() != null) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                Instant lockUntil = now.plus(lockoutDuration);
                user.setLockedUntil(lockUntil);
                repository.save(user);
                throw new AccountLockedException(lockoutDuration.toSeconds());
            }
            repository.save(user);
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        User saved = repository.save(user);

        JwtService.IssuedToken issued = jwtService.issue(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getRole().name());
        String userName = (saved.getFirstName() + " " + saved.getLastName()).trim();
        return new SignInResponseDTO(issued.token(), saved.getRole(), userName, saved.getEmail(), issued.expiresAt());
    }

    @SuppressWarnings("null")
    public User requireById(String userId) {
        return repository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }

    private void rejectPasswordContainingIdentity(String password, String firstName, String email) {
        String pw = password.toLowerCase(Locale.ROOT);
        if (!firstName.isBlank() && pw.contains(firstName.toLowerCase(Locale.ROOT))) {
            throw new WeakPasswordException("Password must not contain your first name");
        }
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        if (!localPart.isBlank() && pw.contains(localPart)) {
            throw new WeakPasswordException("Password must not contain your email");
        }
    }
}

