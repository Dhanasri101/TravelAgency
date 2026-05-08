package com.epam.edp.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.epam.edp.demo.enums.Role;

import java.time.Instant;

@Document("users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private Role role;

    // Travel Agent contact info (null for CUSTOMER role)
    private String phone;
    private String messengerLink;

    private int failedLoginAttempts;
    private Instant lockedUntil;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public User(String firstName, String lastName, String email, String passwordHash) {
        this(firstName, lastName, email, passwordHash, Role.CUSTOMER);
    }

    public User(String firstName, String lastName, String email, String passwordHash, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Role getRole() {
        return role == null ? Role.CUSTOMER : role;
    }
}

