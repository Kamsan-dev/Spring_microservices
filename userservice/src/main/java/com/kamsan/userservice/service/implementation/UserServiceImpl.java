package com.kamsan.userservice.service.implementation;

import com.kamsan.userservice.domain.UserProperties;
import com.kamsan.userservice.dto.*;
import com.kamsan.userservice.event.Event;
import com.kamsan.userservice.mapper.UserMapper;
import com.kamsan.userservice.model.Role;
import com.kamsan.userservice.model.User;
import com.kamsan.userservice.repository.*;
import com.kamsan.userservice.service.UserService;
import com.kamsan.userservice.sharedkernel.exception.ApiException;
import com.kamsan.userservice.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.kamsan.userservice.enumeration.EventType.RESETPASSWORD;
import static com.kamsan.userservice.enumeration.EventType.USER_CREATED;
import static com.kamsan.userservice.utils.UserUtils.memberId;
import static com.kamsan.userservice.utils.UserUtils.randomUUID;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final PasswordTokenRepository passwordTokenRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final UserProperties userProperties;
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

        loadSecurityDetails(user);
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadUserDTO getUserByPublicId(UUID publicId) {
        var user = this.getUserByUUID(publicId);
        loadSecurityDetails(user);
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
        var user = this.getUserByUUID(updateUserDTO.userPublicId());
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
    @Transactional
    public ReadUserDTO enableMfa(UUID userPublicId) {
        var user = this.getUserByUUID(userPublicId);

        String qrCodeSecret = UserUtils.qrCodeSecret.get();
        String qrCodeImageUri = UserUtils.qrCodeImageUri.apply(qrCodeSecret);

        user.setQrCodeSecret(qrCodeSecret);
        user.setQrCodeImageUri(qrCodeImageUri);
        user.setUsingMfa(true);

        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional
    public ReadUserDTO disableMfa(UUID userPublicId) {
        var user = this.getUserByUUID(userPublicId);
        user.setQrCodeSecret(null);
        user.setQrCodeImageUri(null);
        user.setUsingMfa(false);
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional
    public ReadUserDTO uploadPhoto(UUID userPublicId, MultipartFile file) {
        User user = this.getUserByUUID(userPublicId);
        String imageUrl = photoFunction.apply(user.getImageUrl(), file);
        user.setImageUrl(imageUrl + "?timestamp=" + System.currentTimeMillis());
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional
    public void updatePassword(ChangePasswordDTO changePasswordDTO) {
        User user = this.getUserByUUID(changePasswordDTO.userPublicId());

        if (!Objects.equals(changePasswordDTO.newPassword(), changePasswordDTO.confirmNewPassword())) {
            throw new ApiException("Passwords don't match. Please try again");
        }
        var credential = credentialRepository.findByUserId(user.getUserPublicId()).orElseThrow(
                () -> new ApiException(String.format("Unable to retrieve credential for user with publicId %s",
                        user.getUserPublicId())));

        if (!encoder.matches(changePasswordDTO.currentPassword(), credential.getPassword())) {
            throw new ApiException("Existing password is incorrect. Please try again");
        }
        credential.setPassword(encoder.encode(changePasswordDTO.newPassword()));
    }

    @Override
    public void resetPassword(String email) {
        // Verification email unique
        boolean isEmailUsed = userRepository.existsByEmail(email);
        if (!isEmailUsed) {
            throw new ApiException(String.format("Cannot reset password. Email %s does not exist.",
                    email));
        }
        String token = randomUUID.get().toString();
        publisher.publishEvent(new Event(RESETPASSWORD,
                Map.of("token",
                        token,
                        "email",
                        email)));
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
    @Transactional
    public ReadUserDTO toggleAccountExpired(UUID userPublicId) {
        User user = this.getUserByUUID(userPublicId);
        user.setAccountExpired(!user.isAccountExpired());
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional
    public ReadUserDTO toggleAccountLocked(UUID userPublicId) {
        User user = this.getUserByUUID(userPublicId);
        user.setAccountLocked(!user.isAccountLocked());
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    @Transactional
    public ReadUserDTO toggleAccountEnabled(UUID userPublicId) {
        User user = this.getUserByUUID(userPublicId);
        user.setAccountEnabled(!user.isAccountEnabled());
        return userMapper.userToReadUserDTO(user);
    }

    @Override
    public ReadUserDTO toggleCredentialsExpired(UUID userPublicId) {
        return null;
    }

    @Override
    @Transactional
    public ReadUserDTO updateRole(UUID userPublicId, UUID rolePublicId) {
        User user = this.getUserByUUID(userPublicId);
        Role role = roleRepository.findByRolePublicId(rolePublicId)
                                  .orElseThrow(() -> new ApiException(String.format(
                                          "Unable to retrieve role with public id %s",
                                          rolePublicId)));
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthority());
        userRepository.updateUserRole(user.getUserId(), role.getRoleId());
        return userMapper.userToReadUserDTO(user);
    }

    private User getUserByUUID(UUID publicId) {
        return userRepository.findByUserPublicId(publicId).orElseThrow(
                () -> new ApiException(String.format(
                        "User with public id %s does not exist.",
                        publicId)));
    }

    private void loadSecurityDetails(User user) {
        var details = userRepository.findRoleAndAuthorities(user.getUserPublicId());
        user.setRole(details.getRole());
        user.setAuthorities(details.getAuthorities());
    }

    private final Function<String, String> fileExtension = (fileName) -> {
        return Optional.of(fileName)
                       .filter(name -> name.contains("."))
                       .map(name -> name.substring(fileName.lastIndexOf("."))).orElse(".png");
    };

    private final BiFunction<String, MultipartFile, String> photoFunction = (imageUrl, image) -> {
        try {
            String[] splitUrl = imageUrl.split("/");
            // /server/image/profile.png --> profile.png --> profile
            var fileName = splitUrl[splitUrl.length - 1].split("\\.")[0] + fileExtension.apply(imageUrl);
            var existingImage = Paths.get(userProperties.imagesFolder() + splitUrl[splitUrl.length - 1]);
            var fileStorageLocation = Paths.get(userProperties.imagesFolder()).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            } else {
                Files.deleteIfExists(existingImage);
            }

            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName), REPLACE_EXISTING);

            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/user/image/" + fileName).toUriString();

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    };

}
