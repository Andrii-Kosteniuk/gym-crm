package com.training.gym.service;

import com.training.gym.domain.model.User;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPersistenceMapper mapper;

    @Override
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

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            throw new IncorrectMethodArgumentException("New password must contain at least 6 characters");
        }

        User user = authenticate(username, oldPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IncorrectMethodArgumentException("New password must differ from the current password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        userRepository.updatePassword(user.getId(), encodedPassword);

        log.info("Password changed: username={}", username);
    }
}
