package com.training.gym.mapper;

import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.model.Training;
import com.training.gym.dto.training.CreateTrainingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    Training toTraining(CreateTrainingDto dto);

    TrainingResponseDto toResponseDto(Training training);
}
