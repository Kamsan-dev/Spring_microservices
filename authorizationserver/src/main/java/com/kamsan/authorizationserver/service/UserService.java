package com.kamsan.authorizationserver.service;

import com.kamsan.authorizationserver.model.User;

import java.util.UUID;

public interface UserService {
    User getUserByEmail(String email);

    void resetLoginAttempts(UUID userPublicId);

    void updateLoginAttempts(String email);

    void setLastLogin(Long userId);

    void addLoginDevice(Long userId, String deviceName, String client, String ipAddress);

    boolean isValidQRCode(UUID userPublicId, String code);
}
