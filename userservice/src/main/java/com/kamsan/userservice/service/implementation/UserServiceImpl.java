package com.kamsan.userservice.service.implementation;

import com.kamsan.userservice.dto.*;
import com.kamsan.userservice.repository.UserRepository;
import com.kamsan.userservice.repository.UserSecurityProjection;
import com.kamsan.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public ReadUserDTO getUserByEmail(String email) {
        return null;
    }

    @Override
    public ReadUserDTO getUserByPublicId(UUID publicId) {
        return null;
    }

    @Override
    public UserSecurityProjection getUserSecurityData(UUID publicId) {
        return null;
    }

    @Override
    public ReadUserDTO updateUser(UpdateUserDTO updateUserDTO) {
        return null;
    }

    @Override
    public ReadUserDTO createUser(CreateUserDTO createUserDTO) {
        return null;
    }

    @Override
    public void verifyAccount(String token) {

    }

    @Override
    public ReadUserDTO verifyPasswordToken(String token) {
        return null;
    }

    @Override
    public ReadUserDTO enableMfa(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO disableMfa(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO uploadPhoto(UUID userPublicId, MultipartFile file) {
        return null;
    }

    @Override
    public void updatePassword(ChangePasswordDTO changePasswordDTO) {

    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public void doResetPassword(DoResetPasswordDTO doResetPasswordDTO) {

    }

    @Override
    public List<ReadUserDTO> getUsers() {
        return List.of();
    }

    @Override
    public ReadUserDTO getAssignee(UUID userPublicId) {
        return null;
    }

    @Override
    public CredentialDTO getCredential(UUID userPublicId) {
        return null;
    }

    @Override
    public List<DeviceDTO> getDevices(UUID userPublicId) {
        return List.of();
    }

    @Override
    public ReadUserDTO toggleAccountExpired(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO toggleAccountLocked(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO toggleAccountEnabled(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO toggleCredentialsExpired(UUID userPublicId) {
        return null;
    }

    @Override
    public ReadUserDTO updateRole(UUID userPublicId, String role) {
        return null;
    }
}
