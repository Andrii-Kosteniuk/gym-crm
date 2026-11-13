package com.training.gym.mapper;

import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
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
    @Mapping(target = "type", source = "type")
    TrainingResponseDto toResponseDto(Training training);

    @Mapping(target = "specialization", source = "name")
    TrainingTypeResponseDto toTrainingTypeResponseDto(TrainingType type);
}
