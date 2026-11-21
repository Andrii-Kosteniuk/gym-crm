package com.training.gym.facade;

import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.auth.*;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.auth.ProfileAuthorizationService;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.service.training.TrainingService;
import com.training.gym.service.types.TrainingTypeService;
import com.training.gym.service.user.UserService;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.util.validation.TrainingDtoValidator;
import com.training.gym.web.dto.request.ChangeLoginRequestDto;
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
    private final TrainingTypeService trainingTypeService;
    private final UserService userService;
    private final TraineeDtoValidator traineeValidator;
    private final TrainerDtoValidator trainerValidator;
    private final TrainingDtoValidator trainingValidator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingTypePersistenceMapper trainingTypeMapper;
    private final ProfileAuthorizationService authorizationService;
    private final AuthenticationService authenticationService;
    private final CredentialService credentialService;

    public AuthenticationResponse createTrainee(CreateTraineeDto dto) {
        traineeValidator.validateCreate(dto);

        Trainee trainee = traineeMapper.toTrainee(dto);

        credentialService.verifyIfUserExists(dto.firstName(), dto.lastName());

        RegistrationResult<Trainee> result = traineeService.registerTrainee(trainee);

        String username = result.profile().getUser().getUserName();
        String initialPassword = result.initialPassword();

        return new AuthenticationResponse(username, initialPassword);
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


    public AuthenticationResponse createTrainer(CreateTrainerDto dto) {
        trainerValidator.validateCreate(dto);

        Trainer trainer = trainerMapper.toTrainer(dto);

        RegistrationResult<Trainer> result = trainerService.registerTrainer(trainer);

        return new AuthenticationResponse(
                result.profile().getUser().getUserName(),
                result.initialPassword()
        );
    }

    public TrainerResponseDto updateTrainer(UpdateTrainerDto dto, Credentials credentials) {
        trainerValidator.validateUpdate(dto);

        authorizationService.requireTrainerOwner(credentials, dto.userName());

        Trainer trainer = trainerMapper.toUpdateTrainerDomain(dto);

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

    public void changePassword(ChangeLoginRequestDto dto) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        authenticationService.authenticate(dto.username(), dto.oldPassword());

        userService.changeLogin(dto);
    }

    public void createTraining(CreateTrainingDto dto, Credentials credentials) {

        Objects.requireNonNull(credentials, "Credentials are required");

        authenticationService.authenticate(
                credentials.username(),
                credentials.password());

        trainingValidator.validateCreate(dto);

        Training training = trainingMapper.toTraining(dto);

        trainingMapper.toResponseDto(
                trainingService.createTraining(training)
        );
    }

    public TrainingResponseDto getTrainingById(Long id, Credentials credentials) {
        Objects.requireNonNull(id, ID_CAN_NOT_BE_NULL);

        authenticationService.authenticate(
                credentials.username(),
                credentials.password());

        Training training = trainingService.getTrainingById(id);

        return trainingMapper.toResponseDto(training);
    }

    public void changeTraineeProfileStatus(String username, boolean active, Credentials credentials) {

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.changeProfileStatus(username, active);
    }


    public void changeTrainerProfileStatus(String username, boolean active, Credentials credentials) {

        authorizationService.requireTrainerOwner(credentials, username);

        trainerService.changeProfileStatus(username, active);
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

    public void replaceTraineeTrainers(String username, Set<Long> trainerIds, Credentials credentials) {
        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.replaceTrainers(username, trainerIds);
    }

    public void removeTraineeByUsername(String username, Credentials credentials) {
        Objects.requireNonNull(username, USERNAME_CAN_NOT_BE_NULL);

        authorizationService.requireTraineeOwner(credentials, username);

        traineeService.removeTraineeByUsername(username);
    }

    public void authenticate(Credentials credentials) {

        authenticationService.authenticate(credentials.username(), credentials.password());
    }

    public List<TrainingTypeResponseDto> getTrainingTypes() {

        List<TrainingType> trainingTypes = trainingTypeService.getTrainingTypes();
        return trainingTypes.stream()
                .map(trainingTypeMapper::toTrainingTypeResponse)
                .toList();

    }

    public void createTraining(NewTrainingDto newTrainingDto, Credentials credentials) {
        authenticationService.authenticate(credentials.username(), credentials.password());

        Training training = trainingMapper.toTraining(newTrainingDto);

        trainingService.createTraining(training);
    }
}


