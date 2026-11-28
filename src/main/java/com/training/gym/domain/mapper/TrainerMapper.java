package com.training.gym.domain.mapper;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "specialization.id", source = "specializationId")
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toTrainer(CreateTrainerDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", source = "userName")
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", source = "active")
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    Trainer toUpdateTrainerDomain(UpdateTrainerDto dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "type", source = "specialization")
    @Mapping(target = "trainees", source = "trainees")
    TrainerResponseDto toResponseDto(Trainer trainer);


    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "trainers", source = "trainers")
    TraineeResponseDto toTraineeResponseDto(Trainee trainee);

    List<TraineeResponseDto> trainees(Set<Trainee> trainees);

    TrainingTypeResponseDto toTrainingTypeResponseDto(TrainingType trainingType);
}
