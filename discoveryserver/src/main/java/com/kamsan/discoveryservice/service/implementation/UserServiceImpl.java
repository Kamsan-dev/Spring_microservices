package com.kamsan.discoveryservice.service.implementation;

import com.kamsan.discoveryservice.model.User;
import com.kamsan.discoveryservice.repository.UserRepository;
import com.kamsan.discoveryservice.service.UserService;
import com.kamsan.discoveryservice.sharedkernel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.kamsan.discoveryservice.utils.UserUtils.verifyQrCode;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(String.format("Email address %s does not exist", email)));
    }

    @Override
    public void resetLoginAttempts(UUID userPublicId) {
        userRepository.resetLoginAttempts(userPublicId);
    }

    @Override
    public void updateLoginAttempts(String email) {
        userRepository.updateLoginAttempts(email);
    }

    @Override
    public void setLastLogin(Long userId) {
        userRepository.setLastLogin(userId);
    }

    @Override
    public void addLoginDevice(Long userId, String deviceName, String client, String ipAddress) {
        userRepository.addLoginDevice(userId, deviceName, client, ipAddress);
    }

    @Override
    public boolean isValidQRCode(UUID userPublicId, String code) {
        var user = userRepository.findByPublicId(userPublicId)
                                 .orElseThrow(() -> new ApiException(String.format("User with public id %s does not exist", userPublicId)));
        return verifyQrCode(user.getQrCodeSecret(), code);
    }
}
