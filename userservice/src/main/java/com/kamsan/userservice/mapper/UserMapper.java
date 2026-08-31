package com.kamsan.userservice.mapper;

import com.kamsan.userservice.dto.CredentialDTO;
import com.kamsan.userservice.dto.DeviceDTO;
import com.kamsan.userservice.dto.ReadUserDTO;
import com.kamsan.userservice.model.Credential;
import com.kamsan.userservice.model.Device;
import com.kamsan.userservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ReadUserDTO userToReadUserDTO(User user);

    CredentialDTO credentialToCredentialDTO(Credential credential);

    DeviceDTO deviceToDeviceDTO(Device device);

}