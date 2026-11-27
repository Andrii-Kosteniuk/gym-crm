package com.training.gym.service.mapper;

import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.domain.model.TrainingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypePersistenceMapper {

    TrainingType toTrainingTypeDomain(TrainingTypeEntity entity);

    TrainingTypeResponseDto toTrainingTypeResponse(TrainingType trainingType);
}
