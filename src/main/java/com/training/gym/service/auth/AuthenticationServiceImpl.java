package com.training.gym.service.auth;

import com.training.gym.config.security.bruteforce.LoginAttemptService;
import com.training.gym.config.security.jwt.JwtRevocationService;
import com.training.gym.config.security.jwt.JwtTokenService;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.exception.AccountTemporarilyLockedException;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.request.TraineeRegistrationRequest;
import com.training.gym.web.dto.request.TrainerRegistrationRequest;
import com.training.gym.web.dto.response.AuthResponse;
import com.training.gym.web.dto.response.RegistrationResponse;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainerWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptService loginAttemptService;
    private final TraineeWebMapper traineeWebMapper;
    private final TrainerWebMapper trainerWebMapper;
    private final JwtRevocationService revocationService;

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {

        Duration remainingTime = loginAttemptService.getRemainingLockDuration(request.username());

        if (!remainingTime.isZero()) {
            throw new AccountTemporarilyLockedException(remainingTime);
        }

        String username = request.username();
        String password = request.password();

        log.debug("Authentication attempt: {}", username);

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        } catch (LockedException exception) {

            throw new AccountTemporarilyLockedException(
                    loginAttemptService.getRemainingLockDuration(username));

        } catch (BadCredentialsException exception) {

            if (loginAttemptService.isBlocked(username)) {

                throw new AccountTemporarilyLockedException(
                        loginAttemptService.getRemainingLockDuration(username));
            }

            throw exception;
        }

        log.debug("Authentication successful: {}", authentication.getName());

        String token = jwtTokenService.generateToken(authentication.getName());

        log.debug("JWT successfully generated");

        return new AuthResponse(token);
    }

    @Override
    public RegistrationResponse registerTrainee(TraineeRegistrationRequest request) {

        Trainee trainee = traineeWebMapper.toTrainee(request);

        RegistrationResult<Trainee> result = traineeService.registerTrainee(trainee);

        String username = result.profile().getUser().getUserName();
        String password = result.initialPassword();

        String accessToken = jwtTokenService.generateToken(username);

        return new RegistrationResponse(username, password, accessToken);
    }

    @Override
    public RegistrationResponse registerTrainer(TrainerRegistrationRequest request) {
        Trainer trainer = trainerWebMapper.toTrainer(request);

        RegistrationResult<Trainer> result = trainerService.registerTrainer(trainer);

        String username = result.profile().getUser().getUserName();
        String password = result.initialPassword();

        String accessToken = jwtTokenService.generateToken(username);

        return new RegistrationResponse(username, password, accessToken);
    }

    @Override
    public void logout(Jwt jwt) {

        revocationService.revoke(jwt.getId(), jwt.getExpiresAt());

        log.info("User logged out: username={}", jwt.getSubject());
    }
}
