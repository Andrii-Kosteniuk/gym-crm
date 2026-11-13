package com.training.gym.mapper;

import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    Training toTraining(CreateTrainingDto dto);

    @Mapping(
            target = "duration",
            expression = "java(training.getDuration().toMinutes())"
    )
    TrainingResponseDto toResponseDto(Training training);
}
