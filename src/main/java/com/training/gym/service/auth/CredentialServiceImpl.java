package com.training.gym.service.auth;

import com.training.gym.domain.model.User;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserNotFoundException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
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
    @Transactional
    @PreAuthorize("@profileAuthorizationService.isOwner(authentication, #username)")
    public void changePassword(String username, String oldPassword, String newPassword) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username not found"));

        User user = mapper.toUserDomain(userEntity);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Passwords don't match");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IncorrectMethodArgumentException("New password must contain at least 6 characters");
        }


        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IncorrectMethodArgumentException("New password must differ from the current password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        userRepository.updatePassword(user.getId(), encodedPassword);

        log.info("Password changed: username={}", username);
    }
}
