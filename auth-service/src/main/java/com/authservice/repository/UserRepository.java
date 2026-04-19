package com.authservice.repository;

import com.authservice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository Interface
 * It extends JpaRepository to give us built-in MySQL operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Check if email exists for registration validation
    boolean existsByEmail(String email);
    
    // Used for future Login logic 
    Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);
}