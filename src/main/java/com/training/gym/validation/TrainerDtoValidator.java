package com.training.gym.validation;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

import static com.training.gym.validation.ValidationUtils.*;

@Component
@RequiredArgsConstructor
public class TrainerDtoValidator {

    private final Clock clock;

    public void validateCreate(CreateTrainerDto dto) {
        requireNotNull(dto, "CreateTrainerDto");

        validatePersonalData(
                dto.firstName(),
                dto.lastName(),
                dto.dateOfBirth(),
                dto.specialization()
        );
    }

    public void validateUpdate(UpdateTrainerDto dto) {
        requireNotNull(dto, "UpdateTrainerDto");

        requireNotBlank(dto.userName(), "Username");

        validatePersonalData(
                dto.firstName(),
                dto.lastName(),
                dto.dateOfBirth(),
                dto.specialization()
        );
    }

    private void validatePersonalData(String firstName, String lastName, LocalDate dateOfBirth, TrainingType specialization) {
        requireNotBlank(firstName, "First name");
        requireNotBlank(lastName, "Last name");

        requireValidDateOfBirth(dateOfBirth, LocalDate.now(clock));

        requireNotNull(specialization, "Trainer specialization");
    }
}