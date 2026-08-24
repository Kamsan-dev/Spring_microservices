package com.kamsan.gateway.repository;

import com.kamsan.gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(UUID userPublicId);

    Optional<User> findByUsername(String username);

    void resetLoginAttempts(UUID userPublicId);

    void updateLoginAttempts(String email);

    void setLastLogin(Long userId);

    void addLoginDevice(Long userId, String deviceName, String client, String ipAddress);
}
