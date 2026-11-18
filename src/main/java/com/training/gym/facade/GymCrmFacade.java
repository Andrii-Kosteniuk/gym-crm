package com.training.gym.facade;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.Training;
import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResponseDto;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.service.*;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.util.validation.TrainingDtoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {

    public static final String USERNAME_CAN_NOT_BE_NULL = "Username can not be null";
    public static final String ID_CAN_NOT_BE_NULL = "ID can not be null";

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeDtoValidator traineeValidator;
    private final TrainerDtoValidator trainerValidator;
    private final TrainingDtoValidator trainingValidator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final ProfileAuthorizationService authorizationService;
    private final CredentialService credentialService;

    public RegistrationResponseDto createTrainee(CreateTraineeDto dto) {
        traineeValidator.validateCreate(dto);

        Trainee trainee = traineeMapper.toTrainee(dto);

        RegistrationResult<Trainee> result = traineeService.registerTrainee(trainee);

        Long id = result.profile().getId();
        String username = result.profile().getUser().getUserName();
        String initialPassword = result.initialPassword();

        return new RegistrationResponseDto(id, username, initialPassword);
    }

    public TraineeResponseDto updateTrainee(UpdateTraineeDto dto, Credentials credentials) {

        traineeValidator.validateUpdate(dto);

        authorizationService.requireTraineeOwner(credentials, dto.userName());

        Trainee trainee = traineeMapper.toUpdateTrainee(dto);

        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        return traineeMapper.toResponseDto(updatedTrainee);
    }

    public TraineeResponseDto getTraineeById(Long id, Credentials credentials) {

        authorizationService.requireTraineeOwner(credentials, id);

        Trainee traineeById = traineeService.getTraineeById(id);

        return traineeMapper.toResponseDto(traineeById);
    }

    public TraineeResponseDto getTraineeByUserName(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, username);

        Trainee trainee = traineeService.findByUserName(username);

        return traineeMapper.toResponseDto(trainee);
    }

    public void changeTraineePassword(String username, ChangePasswordDto dto, Credentials credentials) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.changePassword(username, dto.currentPassword(), dto.newPassword());
    }


    public RegistrationResponseDto createTrainer(CreateTrainerDto dto) {
        trainerValidator.validateCreate(dto);

        Trainer trainer = trainerMapper.toTrainer(dto);

        RegistrationResult<Trainer> result = trainerService.registerTrainer(trainer);

        return new RegistrationResponseDto(
                result.profile().getId(),
                result.profile().getUser().getUserName(),
                result.initialPassword()
        );
    }

    public TrainerResponseDto updateTrainer(UpdateTrainerDto dto, Credentials credentials) {
        trainerValidator.validateUpdate(dto);

        authorizationService.requireTrainerOwner(credentials, dto.userName());

        Trainer trainer = trainerMapper.updateTrainer(dto);

        Trainer updatedTrainer = trainerService.updateTrainer(trainer);

        return trainerMapper.toResponseDto(updatedTrainer);
    }

    public TrainerResponseDto getTrainerById(Long id, Credentials credentials) {

        authorizationService.requireTrainerOwner(credentials, id);

        Trainer trainerById = trainerService.getTrainerById(id);

        return trainerMapper.toResponseDto(trainerById);
    }

    public TrainerResponseDto getTrainerByUserName(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTrainerOwner(credentials, username);

        Trainer trainer = trainerService.findByUserName(username);

        return trainerMapper.toResponseDto(trainer);
    }

    public void changeTrainerPassword(String username, ChangePasswordDto dto, Credentials credentials) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        authorizationService.requireTrainerOwner(credentials, username);

        trainerService.changePassword(username, dto.currentPassword(), dto.newPassword());
    }

    public TrainingResponseDto createTraining(CreateTrainingDto dto, Credentials credentials) {

        Objects.requireNonNull(credentials, "Credentials are required");

        credentialService.authenticate(
                credentials.username(),
                credentials.password());

        trainingValidator.validateCreate(dto);

        Training training = trainingMapper.toTraining(dto);

        return trainingMapper.toResponseDto(
                trainingService.createTraining(training)
        );
    }

    public TrainingResponseDto getTrainingById(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        credentialService.authenticate(
                credentials.username(),
                credentials.password());


        Training training = trainingService.getTrainingById(id);

        return trainingMapper.toResponseDto(training);
    }

    public void activateTrainee(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, id);

        Trainee trainee = traineeService.getTraineeById(id);

        traineeService.activateTrainee(trainee);
    }

    public void deActivateTrainee(Long id, Credentials credentials) {

        authorizationService.requireTraineeOwner(credentials, id);

        Trainee trainee = traineeService.getTraineeById(id);

        traineeService.deactivateTrainee(trainee);
    }


    public void activateTrainer(Long id, Credentials credentials) {

        authorizationService.requireTrainerOwner(credentials, id);

        Trainer trainer = trainerService.getTrainerById(id);

        trainerService.activateTrainer(trainer);
    }

    public void deActivateTrainer(Long id, Credentials credentials) {

        authorizationService.requireTrainerOwner(credentials, id);

        Trainer trainer = trainerService.getTrainerById(id);

        trainerService.deactivateTrainer(trainer);
    }

    public List<TrainerResponseDto> getUnassignedTrainers(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, username);

        List<Trainer> trainerEntities = trainerService.getUnassignedTrainers(username);

        return trainerEntities.stream()
                .map(trainerMapper::toResponseDto)
                .toList();
    }

    public List<TrainingResponseDto> getTraineeTrainings(String username, TraineeTrainingCriteria criteria, Credentials credentials) {
        authorizationService.requireTraineeOwner(credentials, username);

        return traineeService.findTrainings(username, criteria)
                .stream()
                .map(trainingMapper::toResponseDto)
                .toList();
    }

    public List<TrainingResponseDto> getTrainerTrainings(String username, TrainerTrainingCriteria criteria, Credentials credentials) {
        authorizationService.requireTrainerOwner(credentials, username);

        return trainerService.findTrainings(username, criteria)
                .stream()
                .map(trainingMapper::toResponseDto)
                .toList();
    }

    public void replaceTraineeTrainers(Long traineeId, Set<Long> trainerIds, Credentials credentials) {
        authorizationService.requireTraineeOwner(credentials, traineeId);

        traineeService.replaceTrainers(traineeId, trainerIds);
    }

    public void removeTraineeByUsername(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.removeTraineeByUsername(username);
    }

}


