package com.kamsan.authorizationserver.service;

import com.kamsan.authorizationserver.model.User;
import com.kamsan.authorizationserver.repository.UserSecurityProjection;

import java.util.UUID;

public interface UserService {
    User getUserByEmail(String email);

    UserSecurityProjection getUserSecurityData(UUID publicId);

    void resetLoginAttempts(UUID userPublicId);

    void updateLoginAttempts(String email);

    void setLastLogin(Long userId);

    void addLoginDevice(Long userId, String deviceName, String client, String ipAddress);

    boolean isValidQRCode(UUID userPublicId, String code);
}
