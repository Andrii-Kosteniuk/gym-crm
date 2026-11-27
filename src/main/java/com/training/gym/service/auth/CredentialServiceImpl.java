package com.training.gym.service.auth;

import com.training.gym.domain.model.User;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import com.training.gym.web.dto.request.ChangeLoginRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void changePassword(String username, String oldPassword, String newPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            throw new IncorrectMethodArgumentException("New password must contain at least 6 characters");
        }
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow();

        User user = mapper.toUserDomain(userEntity);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IncorrectMethodArgumentException("New password must differ from the current password");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Passwords don't match");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        userRepository.updatePassword(user.getId(), encodedPassword);

        log.info("Password changed: username={}", username);
    }

    public void verifyIfUserExists(String firstName, String lastName) {

       boolean foundUser = userRepository.existsByFirstNameAndLastName(firstName, lastName);

        if (foundUser) throw new BusinessRuleViolationException("User with such name already exists");
    }


}
