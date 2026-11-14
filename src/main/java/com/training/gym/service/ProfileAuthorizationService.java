package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileAuthorizationService {

    private final CredentialService credentialService;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public Trainee requireTrainee(Credentials credentials) {
        verifyCredentials(credentials);

        User user = credentialService.authenticate(
                credentials.username(),
                credentials.password());

        return traineeRepository.findByUserName(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }

    public Trainer requireTrainer(Credentials credentials) {
        verifyCredentials(credentials);

        User user = credentialService.authenticate(
                credentials.username(),
                credentials.password()
        );

        return trainerRepository.findByUserName(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }

    public void requireTraineeOwner(Credentials credentials, String username) {
        verifyCredentials(credentials);

        Trainee trainee = requireTrainee(credentials);

        if (!trainee.getUser().getUserName().equals(username)) {
            throw new AccessDeniedException("Not authorized to modify this trainee");
        }

    }

    public void requireTrainerOwner(Credentials credentials, String username) {
        verifyCredentials(credentials);

        Trainer trainer = requireTrainer(credentials);

        if (!trainer.getUser().getUserName().equals(username)) {
            throw new AccessDeniedException("Not authorized to modify this trainer");
        }

    }

    private void verifyCredentials(Credentials credentials) {
        Objects.requireNonNull(credentials, "Credentials must not be null");
        if (credentials.username() == null || credentials.username().isBlank()) {
            throw new AuthenticationFailedException();
        }
    }
}