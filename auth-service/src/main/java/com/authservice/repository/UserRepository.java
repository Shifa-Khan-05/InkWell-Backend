package com.authservice.repository;

import com.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	Optional<User> findByResetToken(String token);
	java.util.List<User> findByRole(String role);
}