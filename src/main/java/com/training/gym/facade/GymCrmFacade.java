package com.training.gym.facade;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResponseDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.service.*;
import com.training.gym.validation.TraineeDtoValidator;
import com.training.gym.validation.TrainerDtoValidator;
import com.training.gym.validation.TrainingDtoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

    public RegistrationResponseDto createTrainee(CreateTraineeDto dto) {
        traineeValidator.validateCreate(dto);

        Trainee trainee = traineeMapper.toTrainee(dto);

        RegistrationResult<Trainee> result = traineeService.registerTrainee(trainee);

        return new RegistrationResponseDto(
                result.profile().getId(),
                result.profile().getUser().getUserName(),
                result.initialPassword()
        );
    }

    public TraineeResponseDto updateTrainee(UpdateTraineeDto dto, Credentials credentials) {

        traineeValidator.validateUpdate(dto);

        authorizationService.requireTraineeOwner(credentials, dto.userName());

        Trainee trainee = traineeMapper.toUpdateTrainee(dto);

        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        return traineeMapper.toResponseDto(updatedTrainee);
    }

    public TraineeResponseDto getTraineeById(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainee traineeById = traineeService.getTraineeById(id);
        String username = traineeById.getUser().getUserName();

        authorizationService.requireTraineeOwner(credentials, username);

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
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainer trainerById = trainerService.getTrainerById(id);
        String username = trainerById.getUser().getUserName();

        authorizationService.requireTrainerOwner(credentials, username);

        return trainerMapper.toResponseDto(trainerById);
    }

    public TrainerResponseDto getTrainerByUserName(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        Trainer trainer = trainerService.findByUserName(username);

        authorizationService.requireTrainerOwner(credentials, username);

        return trainerMapper.toResponseDto(trainer);
    }

    public void changeTrainerPassword(String username, ChangePasswordDto dto, Credentials credentials) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        authorizationService.requireTrainerOwner(credentials, username);

        trainerService.changePassword(username, dto.currentPassword(), dto.newPassword());
    }

    public TrainingResponseDto createTraining(CreateTrainingDto dto) {
        trainingValidator.validateCreate(dto);

        Training training = trainingMapper.toTraining(dto);

        Training createdTraining = trainingService.createTraining(training);
        return trainingMapper.toResponseDto(createdTraining);
    }

    public TrainingResponseDto getTrainingById(Long id) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Training training = trainingService.getTrainingById(id);

        return trainingMapper.toResponseDto(training);
    }

    public void activateTrainee(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainee trainee = traineeService.getTraineeById(id);
        String username = trainee.getUser().getUserName();

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.activateTrainee(trainee);
    }

    public void deActivateTrainee(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainee trainee = traineeService.getTraineeById(id);
        String username = trainee.getUser().getUserName();

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.deactivateTrainee(trainee);
    }


    public void activateTrainer(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainer trainer = trainerService.getTrainerById(id);
        String username = trainer.getUser().getUserName();

        authorizationService.requireTrainerOwner(credentials, username);

        trainerService.activateTrainer(trainer);
    }

    public void deActivateTrainer(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        Trainer trainer = trainerService.getTrainerById(id);
        String username = trainer.getUser().getUserName();

        authorizationService.requireTrainerOwner(credentials, username);

        trainerService.deactivateTrainer(trainer);
    }

    public List<TrainerResponseDto> getUnassignedTrainers(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTrainerOwner(credentials, username);

        List<Trainer> trainers = trainerService.getUnassignedTrainers(username);

        return trainers.stream()
                .map(trainerMapper::toResponseDto)
                .toList();
    }

    public void authenticateTrainee(Credentials credentials) {
        authorizationService.requireTrainee(credentials);
    }

    public void authenticateTrainer(Credentials credentials) {
        authorizationService.requireTrainer(credentials);
    }

    public void removeTraineeByUsername(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.removeTraineeByUsername(username);
    }

}


