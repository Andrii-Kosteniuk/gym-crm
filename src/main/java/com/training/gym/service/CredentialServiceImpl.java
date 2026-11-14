package com.training.gym.service;

import com.training.gym.dao.UserRepository;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.User;
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

    @Override
    public User authenticate(String username, String password) {
        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {
            throw new AuthenticationFailedException();
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(AuthenticationFailedException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        return user;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must contain at least 6 characters" );
        }

        User user = authenticate(username, oldPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        log.info("Password changed: username={}", username);
    }
}
