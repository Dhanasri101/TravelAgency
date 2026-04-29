package com.epam.edp.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.edp.demo.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}

