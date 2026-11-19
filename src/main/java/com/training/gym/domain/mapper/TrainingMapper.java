package com.training.gym.domain.mapper;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainee", source = "traineeId")
    @Mapping(target = "trainer", source = "trainerId")
    @Mapping(target = "trainingType", source = "trainingTypeId")
    @Mapping(target = "trainingName", source = "name")
    @Mapping(target = "trainingDate", source = "date")
    @Mapping(target = "trainingDuration", source = "duration")
    Training toTraining(CreateTrainingDto dto);

    @Mapping(target = "traineeId", source = "trainee.id")
    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "type", source = "trainingType")
    @Mapping(target = "duration", source = "trainingDuration")
    TrainingResponseDto toResponseDto(Training training);

    @Mapping(target = "specialization", source = "name")
    TrainingTypeResponseDto toTrainingTypeResponseDto(TrainingType type);

    default Trainee mapTrainee(Long id) {
        if (id == null) {
            return null;
        }

        Trainee trainee = new Trainee();
        trainee.setId(id);
        return trainee;
    }

    default Trainer mapTrainer(Long id) {
        if (id == null) {
            return null;
        }

        Trainer trainer = new Trainer();
        trainer.setId(id);
        return trainer;
    }

    default TrainingType mapTrainingType(Long id) {
        return id == null
                ? null
                : TrainingType.builder().id(id).build();
    }

    default Duration mapDuration(Long minutes) {
        return minutes == null
                ? null
                : Duration.ofMinutes(minutes);
    }

    default Long map(Duration duration) {
        return duration == null
                ? null
                : duration.toMinutes();
    }
}