package com.training.gym.persistence.mapper;

import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.domain.TrainingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypePersistenceMapper {

    TrainingType toTrainingDomain(TrainingTypeEntity entity);
}
