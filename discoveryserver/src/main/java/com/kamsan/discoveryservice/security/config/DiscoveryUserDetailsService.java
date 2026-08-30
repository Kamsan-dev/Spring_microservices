package com.kamsan.discoveryservice.security.config;

import com.kamsan.discoveryservice.repository.UserRepository;
import com.kamsan.discoveryservice.sharedkernel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                                 .orElseThrow(() -> new UsernameNotFoundException(String.format(
                                         "Username %s does not exist.",
                                         username)));

        var userSecurityData = userRepository.findSecurityDataByPublicId(user.getUserPublicId())
                                             .orElseThrow(() -> new ApiException(String.format(
                                                     "User with public id %s does not exist.",
                                                     user.getUserPublicId())));
        
        return new User(user.getUsername(), userSecurityData.getPassword(), user.isAccountEnabled(),
                !user.isAccountExpired(), !userSecurityData.getCredentialsExpired(), !user.isAccountLocked(),
                commaSeparatedStringToAuthorityList(userSecurityData.getRole() + "," + userSecurityData.getAuthorities()));
    }
}
