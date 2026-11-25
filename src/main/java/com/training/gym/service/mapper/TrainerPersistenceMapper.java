package com.training.gym.service.mapper;

import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerPersistenceMapper {

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toTrainerDomain(TrainerEntity entity);

    @Mapping(target = "userEntity", source = "user")
    @Mapping(target = "traineeEntities", ignore = true)
    @Mapping(target = "trainingEntities", ignore = true)
    TrainerEntity toEntity(Trainer trainer);

    TrainingTypeEntity toTrainingTypeEntity(TrainingType trainingType);
}
