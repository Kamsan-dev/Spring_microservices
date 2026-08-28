package com.kamsan.discoveryservice.repository;

import com.kamsan.discoveryservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = """
            SELECT  c.password AS password,
                    r.name AS role,
                    r.authority AS authorities,
                    c.updated_at + INTERVAL '90 days' < NOW() AS credentials_expired
                    FROM users u
                    JOIN user_roles ur ON ur.user_id = u.user_id
                    JOIN roles r ON r.role_id = ur.role_id
                    JOIN credentials c ON c.user_id = u.user_id
                    WHERE u.user_public_id = :publicId
            """, nativeQuery = true)
    Optional<UserSecurityProjection> findSecurityDataByPublicId(UUID publicId);
}
