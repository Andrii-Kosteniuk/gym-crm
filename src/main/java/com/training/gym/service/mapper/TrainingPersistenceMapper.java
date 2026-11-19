package com.training.gym.service.mapper;

import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.domain.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
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
}
