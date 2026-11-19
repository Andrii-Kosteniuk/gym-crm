package com.training.gym.service.mapper;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.domain.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TrainingPersistenceMapper {

    @Mapping(target = "trainee", source = "traineeEntity")
    @Mapping(target = "trainer", source = "trainerEntity")
    @Mapping(target = "trainingType", source = "trainingTypeEntity")
    Training toTrainingDomain(TrainingEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "traineeEntity", source = "trainee")
    @Mapping(target = "trainerEntity", source = "trainer")
    @Mapping(target = "trainingTypeEntity", source = "trainingType")
    TrainingEntity toTrainingEntity(Training training);

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "address", source = "addressEntity")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toTrainee(TraineeEntity entity);

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toTrainer(TrainerEntity entity);

    @Mapping(target = "userEntity", source = "user")
    @Mapping(target = "addressEntity", source = "address")
    @Mapping(target = "trainerEntities", ignore = true)
    @Mapping(target = "trainingEntities", ignore = true)
    TraineeEntity toTraineeEntity(Trainee trainee);

    @Mapping(target = "userEntity", source = "user")
    @Mapping(target = "traineeEntities", ignore = true)
    @Mapping(target = "trainingEntities", ignore = true)
    TrainerEntity toTrainerEntity(Trainer trainer);
}
