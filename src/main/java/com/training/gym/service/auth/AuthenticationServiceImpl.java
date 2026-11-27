package com.training.gym.service.auth;

import com.training.gym.domain.model.User;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPersistenceMapper mapper;

    @Override
    @Transactional
    public User authenticate(String username, String password) {
        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {
            throw new AuthenticationFailedException();
        }

        UserEntity user = userRepository
                .findByUsername(username)
                .orElseThrow(AuthenticationFailedException::new);

        if (Boolean.FALSE.equals(user.getActive()))
            throw new AuthenticationFailedException("You are not authenticated");

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        return mapper.toUserDomain(user);
    }
}
