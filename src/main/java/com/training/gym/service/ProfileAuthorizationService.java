package com.training.gym.service;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.User;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileAuthorizationService {

    private final CredentialService credentialService;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;


    public void requireTraineeOwner(Credentials credentials, Long traineeId) {

        Trainee authenticated = requireTrainee(credentials);

        if (!Objects.equals(authenticated.getId(), traineeId)) {
            throw new AccessDeniedException("Access to trainee profile denied");
        }

    }

    public void requireTraineeOwner(Credentials credentials, String username) {
        Trainee authenticated = requireTrainee(credentials);

        verifyOwnership(authenticated.getUser().getUserName(), username);

    }

    public void requireTrainerOwner(Credentials credentials, Long trainerId) {

        Trainer authenticated = requireTrainer(credentials);

        if (!Objects.equals(authenticated.getId(), trainerId)) {
            throw new AccessDeniedException("Access to trainer profile denied");
        }

    }

    public void requireTrainerOwner(Credentials credentials, String username) {
        Trainer authenticated = requireTrainer(credentials);

        verifyOwnership(authenticated.getUser().getUserName(), username);

    }

    private Trainee requireTrainee(Credentials credentials) {
        return requireProfile(credentials, traineeRepository::findByUserName);
    }

    private Trainer requireTrainer(Credentials credentials) {
        return requireProfile(credentials, trainerRepository::findByUserName);
    }

    private void verifyOwnership(String authenticatedUsername, String requestedUsername) {
        if (!authenticatedUsername.equals(requestedUsername)) {
            throw new AccessDeniedException("Access to profile denied");
        }
    }

    private void verifyCredentials(Credentials credentials) {
        if (credentials == null
                || credentials.username() == null
                || credentials.username().isBlank()
                || credentials.password() == null
                || credentials.password().isBlank()) {
            throw new AuthenticationFailedException();
        }
    }

    private <T> T requireProfile(Credentials credentials, Function<String, Optional<T>> profile) {
        verifyCredentials(credentials);

        User user = credentialService.authenticate(
                credentials.username(),
                credentials.password()
        );

        return profile.apply(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }
}