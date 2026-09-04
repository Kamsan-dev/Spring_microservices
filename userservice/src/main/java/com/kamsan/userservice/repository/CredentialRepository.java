package com.kamsan.userservice.repository;

import com.kamsan.userservice.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByUserId(Long userId);

    @Query(value = """
            SELECT
            c.credential_public_id,
            c.password,
            c.created_at,
            c.updated_at,
            (c.created_at + INTERVAL '24 HOURS') < NOW() as is_expired
            FROM credentials c
            JOIN users u
            ON u.user_id = c.user_id
            WHERE u.user_public_id = :publicId
            """, nativeQuery = true)
    Optional<Credential> findByUserPublicId(UUID publicId);

}