package com.authservice.repository;

import com.authservice.entity.RoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest, Integer> {
    List<RoleRequest> findByStatus(String status);
    List<RoleRequest> findByUserUserId(int userId);
    boolean existsByUserUserIdAndStatus(int userId, String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM RoleRequest r WHERE r.user.userId = :userId")
    void deleteByUserUserId(int userId);
}
