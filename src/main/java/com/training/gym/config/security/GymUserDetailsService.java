package com.training.gym.config.security;

import com.training.gym.config.security.bruteforce.LoginAttemptService;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@RequiredArgsConstructor
@Slf4j
public class GymUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPersistenceMapper mapper;
    private final LoginAttemptService loginAttemptService;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username %s not found".formatted(username)));

        var userDomain = mapper.toUserDomain(userEntity);

        boolean blocked = loginAttemptService.isBlocked(username);

        log.debug("Loading user: username={}, blocked={}", username, blocked);

        return User.builder()
                .username(userDomain.getUserName())
                .password(userDomain.getPassword())
                .disabled(!userDomain.getActive())
                .accountLocked(blocked)
                .authorities(Collections.emptyList())
                .build();
    }
}
