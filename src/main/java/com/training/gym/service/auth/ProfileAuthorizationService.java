package com.training.gym.service.auth;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.User;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
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

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TraineePersistenceMapper traineeMapper;
    private final TrainerPersistenceMapper trainerMapper;
    private final AuthenticationService authenticationService;


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
        TraineeEntity traineeEntity = requireProfile(credentials, traineeRepository::findByUserName);

        return traineeMapper.toTraineeDomain(traineeEntity);
    }

    private Trainer requireTrainer(Credentials credentials) {
        TrainerEntity trainerEntity = requireProfile(credentials, trainerRepository::findByUserName);

        return trainerMapper.toTrainerDomain(trainerEntity);
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

        User user = authenticationService.authenticate(
                credentials.username(),
                credentials.password()
        );

        return profile.apply(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }
}