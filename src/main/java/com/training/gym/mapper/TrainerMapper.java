package com.training.gym.mapper;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.model.Trainer;
import com.training.gym.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    Trainer toTrainer(CreateTrainerDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "password", ignore = true)
    Trainer updateTrainer(UpdateTrainerDto dto);

    @Mapping(target = "type", source = "specialization")
    TrainerResponseDto toResponseDto(Trainer trainer);

    @Mapping(target = "specialization", source = "name")
    TrainingTypeResponseDto toTrainingTypeResponseDto(TrainingType type);
}
