package com.training.gym.util.validation;

import com.training.gym.dto.training.CreateTrainingDto;
import org.springframework.stereotype.Component;

import static com.training.gym.util.validation.ValidationUtils.*;

@Component
public class TrainingDtoValidator {

    public void validateCreate(CreateTrainingDto dto) {
        requireNotNull(dto, "CreateTrainingDto");

        requirePositiveId(dto.traineeId(), "Trainee ID");
        requirePositiveId(dto.trainerId(), "Trainer ID");

        requireNotBlank(dto.name(), "Training name");

        requirePositiveId(dto.trainingTypeId(), "Training type id");
        requireNotNull(dto.date(), "Training date");

        requirePositiveDuration(dto.duration());
    }
}