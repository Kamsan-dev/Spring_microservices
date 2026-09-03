package com.kamsan.userservice.repository;

import com.kamsan.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserPublicId(UUID userPublicId);

    Optional<User> findByUserId(Long id);

    boolean existsByEmail(String email);

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

    @Query(value = """
            SELECT  r.name AS role,
                    r.authority AS authorities
                    FROM users u
                    JOIN user_roles ur ON ur.user_id = u.user_id
                    JOIN roles r ON r.role_id = ur.role_id
                    WHERE u.user_public_id = :publicId
            """, nativeQuery = true)
    UserRoleAndAuthoritiesProjection findRoleAndAuthorities(UUID publicId);

    @Procedure(procedureName = "public.create_user")
    void createUser(
            @Param("p_email") String email,
            @Param("p_password") String password,
            @Param("p_first_name") String firstName,
            @Param("p_last_name") String lastName,
            @Param("p_username") String username,
            @Param("p_public_id") String userPublicId,
            @Param("p_credential_public_id") String credentialPublicId,
            @Param("p_token") String token,
            @Param("p_member_id") String memberId
    );

    @Query(value = """
            UPDATE users SET is_account_enabled = TRUE WHERE user_id = :userId
            """, nativeQuery = true)
    void updateUserSettings(Long userId);

    @Query(value = """
            UPDATE user_roles ur SET ur.role_id = :roleId
            WHERE ur.user_id = :userId
            """, nativeQuery = true)
    void updateUserRole(Long userId, Long roleId);

}
