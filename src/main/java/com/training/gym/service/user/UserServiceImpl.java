package com.training.gym.service.user;

import com.training.gym.web.dto.request.ChangeLoginRequestDto;
import com.training.gym.exception.BadCredentialsException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changeLogin(ChangeLoginRequestDto request) {

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(BadCredentialsException::new);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

}
