package com.training.gym.persistence.mapper;

import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.domain.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TrainingPersistenceMapper {

    @Mapping(target = "trainee", source = "traineeEntity")
    @Mapping(target = "trainer", source = "trainerEntity")
    @Mapping(target = "trainingType", source = "trainingTypeEntity")
    Training toTrainingDomain(TrainingEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "traineeEntity", ignore = true)
    @Mapping(target = "trainerEntity", ignore = true)
    @Mapping(target = "trainingTypeEntity", ignore = true)
    TrainingEntity toTrainingEntity(Training training);
}
