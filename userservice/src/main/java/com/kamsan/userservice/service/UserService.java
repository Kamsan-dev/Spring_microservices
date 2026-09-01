package com.kamsan.userservice.service;

import com.kamsan.userservice.dto.*;
import com.kamsan.userservice.repository.UserSecurityProjection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    ReadUserDTO getUserByEmail(String email);

    ReadUserDTO getUserByPublicId(UUID publicId);

    UserSecurityProjection getUserSecurityData(UUID publicId);

    ReadUserDTO updateUser(UpdateUserDTO updateUserDTO);

    void createUser(CreateUserDTO createUserDTO);

    void verifyAccount(String token);

    ReadUserDTO verifyPasswordToken(String token);

    ReadUserDTO enableMfa(UUID userPublicId);

    ReadUserDTO disableMfa(UUID userPublicId);

    ReadUserDTO uploadPhoto(UUID userPublicId, MultipartFile file);

    void updatePassword(ChangePasswordDTO changePasswordDTO);

    void resetPassword(String email);

    void doResetPassword(DoResetPasswordDTO doResetPasswordDTO);

    List<ReadUserDTO> getUsers();

    ReadUserDTO getAssignee(UUID userPublicId);

    CredentialDTO getCredential(UUID userPublicId);

    List<DeviceDTO> getDevices(UUID userPublicId);

    /**
     * Admin
     **/

    ReadUserDTO toggleAccountExpired(UUID userPublicId);

    ReadUserDTO toggleAccountLocked(UUID userPublicId);

    ReadUserDTO toggleAccountEnabled(UUID userPublicId);

    ReadUserDTO toggleCredentialsExpired(UUID userPublicId);

    ReadUserDTO updateRole(UUID userPublicId, String role);

}
