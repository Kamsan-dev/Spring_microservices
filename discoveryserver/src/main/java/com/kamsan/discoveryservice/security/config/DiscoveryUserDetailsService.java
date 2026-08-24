package com.kamsan.discoveryservice.security.config;

import com.kamsan.discoveryservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
public class DiscoveryUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                                 .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s does not exist.", username)));

        return new User(user.getUsername(), user.getPassword(), user.isAccountEnabled(),
                !user.isAccountExpired(), !user.isCredentialsExpired(), !user.isAccountLocked(),
                commaSeparatedStringToAuthorityList(user.getRole() + "," + user.getAuthorities()));
    }
}
