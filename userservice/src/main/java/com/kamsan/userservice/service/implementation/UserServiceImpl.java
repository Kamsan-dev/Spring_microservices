package com.kamsan.userservice.service.implementation;

import com.kamsan.userservice.dto.*;
import com.kamsan.userservice.event.Event;
import com.kamsan.userservice.mapper.UserMapper;
import com.kamsan.userservice.model.User;
import com.kamsan.userservice.repository.AccountTokenRepository;
import com.kamsan.userservice.repository.PasswordTokenRepository;
import com.kamsan.userservice.repository.UserRepository;
import com.kamsan.userservice.repository.UserSecurityProjection;
import com.kamsan.userservice.service.UserService;
import com.kamsan.userservice.sharedkernel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.kamsan.userservice.enumeration.EventType.USER_CREATED;
import static com.kamsan.userservice.utils.UserUtils.memberId;
import static com.kamsan.userservice.utils.UserUtils.randomUUID;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final PasswordTokenRepository passwordTokenRepository;
    private final UserMapper userMapper;
    private BCryptPasswordEncoder encoder;
    private ApplicationEventPublisher publisher;
    @Value("${ui.app.url}")
    private String upAppUrl;

    @Override
    @Transactional(readOnly = true)
    public ReadUserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(String.format("Email address %s does not exist", email)));

        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadUserDTO getUserByPublicId(UUID publicId) {
        User user = userRepository.findByUserPublicId(publicId).orElseThrow(
                () -> new ApiException(String.format(
                        "User with public id %s does not exist.",
                        publicId)));

        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSecurityProjection getUserSecurityData(UUID publicId) {
        return userRepository.findSecurityDataByPublicId(publicId)
                             .orElseThrow(() -> new ApiException(String.format(
                                     "User with public id %s does not exist.",
                                     publicId)));
    }

    @Override
    @Transactional
    public ReadUserDTO updateUser(UpdateUserDTO updateUserDTO) {
        UUID userPublicId = updateUserDTO.userPublicId();
        var user = userRepository.findByUserPublicId(userPublicId).orElseThrow(
                () -> new ApiException(String.format(
                        "User with public id %s does not exist.",
                        userPublicId)));

        userMapper.updateUser(updateUserDTO, user);
        return userMapper.userToReadUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    @Modifying(clearAutomatically = true)
    public void createUser(CreateUserDTO createUserDTO) {
        // Verification email unique
        boolean isEmailUsed = userRepository.existsByEmail(createUserDTO.email());
        if (isEmailUsed) {
            throw new ApiException(String.format("Cannot create user. Email %s is already used",
                    createUserDTO.email()));
        }
        String token = callCreateUserProcedure(createUserDTO);
        publisher.publishEvent(new Event(USER_CREATED,
                Map.of("token",
                        token,
                        "email",
                        createUserDTO.email(),
                        "name",
                        capitalizeFully(createUserDTO.firstName()))));
    }

    private String callCreateUserProcedure(CreateUserDTO createUserDTO) {
        User newUser = userMapper.createUserDTOToUser(createUserDTO);
        newUser.setUserPublicId(randomUUID.get());
        newUser.setMemberId(memberId.get());
        UUID credentialPublicId = randomUUID.get();
        UUID token = randomUUID.get();

        userRepository.createUser(newUser.getEmail(),
                newUser.getPassword(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getUsername(),
                newUser.getUserPublicId().toString(),
                credentialPublicId.toString(),
                token.toString(),
                newUser.getMemberId());
        return token.toString();
    }

    @Override
    @Transactional
    public void verifyAccount(String token) {
        var accountToken = accountTokenRepository.findByToken(token)
                                                 .orElseThrow(() -> new ApiException(
                                                         "Invalid link. Please try again."));
        if (accountToken.isExpired()) {
            throw new ApiException("Link has expired. Please try again.");
        }
        userRepository.updateUserSettings(accountToken.getUserId());
        accountTokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public ReadUserDTO verifyPasswordToken(String token) {
        var passwordToken = passwordTokenRepository.findByToken(token)
                                                   .orElseThrow(() -> new ApiException(
                                                           "Invalid link. Please try again."));

        if (passwordToken.isExpired()) {
            throw new ApiException("Link has expired. Please try again.");
        }
        passwordTokenRepository.deleteByToken(token);
        var user = userRepository.findByUserId(passwordToken.getUserId())
                                 .orElseThrow(() -> new ApiException(String.format(
                                         "User with id %s does not exist.",
                                         passwordToken.getUserId())));
        return userMapper.userToReadUserDTO(user);
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
