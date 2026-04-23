package com.newsletterservice.repository;

import com.newsletterservice.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    List<Subscriber> findByActiveTrue();
}