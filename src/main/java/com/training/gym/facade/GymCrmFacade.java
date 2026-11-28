package com.training.gym.facade;

import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.service.training.TrainingService;
import com.training.gym.service.types.TrainingTypeService;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.web.dto.request.*;
import com.training.gym.web.dto.response.AuthResponse;
import com.training.gym.web.dto.response.RegistrationResponse;
import com.training.gym.web.mapper.TrainingWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {

    public static final String USERNAME_CAN_NOT_BE_NULL = "Username can not be null";

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final TraineeDtoValidator traineeValidator;
    private final TrainerDtoValidator trainerValidator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingWebMapper trainingWebMapper;
    private final TrainingTypePersistenceMapper trainingTypeMapper;
    private final AuthenticationService authenticationService;
    private final CredentialService credentialService;

    public RegistrationResponse registerTrainee(TraineeRegistrationRequest request) {

        return authenticationService.registerTrainee(request);
    }

    public TraineeResponseDto updateTrainee(UpdateTraineeDto dto) {

        traineeValidator.validateUpdate(dto);

        Trainee trainee = traineeMapper.toUpdateTrainee(dto);

        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        return traineeMapper.toResponseDto(updatedTrainee);
    }

    public TraineeResponseDto getTraineeByUserName(String username) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        Trainee trainee = traineeService.findByUserName(username);

        return traineeMapper.toResponseDto(trainee);
    }

    public RegistrationResponse registerTrainer(TrainerRegistrationRequest request) {

        return authenticationService.registerTrainer(request);

    }

    public void logOut(Jwt jwt) {
        authenticationService.logout(jwt);
    }

    public List<TrainerResponseDto> replaceTraineeTrainersByUsername(String traineeUsername, Set<String> trainerUsernames) {

        traineeService.replaceTrainersByUsername(traineeUsername, trainerUsernames);

        Trainee trainee = traineeService.findByUserName(traineeUsername);

        return trainee.getTrainers()
                .stream()
                .map(trainerMapper::toResponseDto)
                .toList();
    }

    public TrainerResponseDto updateTrainer(UpdateTrainerDto dto) {
        trainerValidator.validateUpdate(dto);

        Trainer trainer = trainerMapper.toUpdateTrainerDomain(dto);

        Trainer updatedTrainer = trainerService.updateTrainer(trainer);

        return trainerMapper.toResponseDto(updatedTrainer);
    }

    public TrainerResponseDto getTrainerByUserName(String username) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        Trainer trainer = trainerService.findByUserName(username);

        return trainerMapper.toResponseDto(trainer);
    }

    public void changePassword(ChangeLoginRequestDto dto) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        credentialService.changePassword(dto.username(), dto.oldPassword(), dto.newPassword());
    }

    public void changeTraineeProfileStatus(String username, boolean active) {

        traineeService.changeProfileStatus(username, active);
    }


    public void changeTrainerProfileStatus(String username, boolean active) {

        trainerService.changeProfileStatus(username, active);
    }


    public List<TrainerResponseDto> getUnassignedTrainers(String username) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        List<Trainer> trainerEntities = trainerService.getUnassignedTrainers(username);

        return trainerEntities.stream()
                .map(trainerMapper::toResponseDto)
                .toList();
    }


    public List<TrainingResponseDto> getTraineeTrainings(String username, TraineeTrainingCriteria criteria) {

        return traineeService.findTrainings(username, criteria)
                .stream()
                .map(trainingMapper::toResponseDto)
                .toList();
    }

    public List<TrainingResponseDto> getTrainerTrainings(String username, TrainerTrainingCriteria criteria) {

        return trainerService.findTrainings(username, criteria)
                .stream()
                .map(trainingMapper::toResponseDto)
                .toList();
    }

    public void removeTraineeByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        traineeService.removeTraineeByUsername(username);
    }

    public AuthResponse authenticate(AuthRequest request) {

        return authenticationService.authenticate(request);
    }

    public List<TrainingTypeResponseDto> getTrainingTypes() {

        List<TrainingType> trainingTypes = trainingTypeService.getTrainingTypes();
        return trainingTypes.stream()
                .map(trainingTypeMapper::toTrainingTypeResponse)
                .toList();

    }

    public void createTraining(CreateTrainingRequest request) {

        Training training = trainingWebMapper.toTrainingDomain(request);

        trainingService.createTraining(training);
    }
}


