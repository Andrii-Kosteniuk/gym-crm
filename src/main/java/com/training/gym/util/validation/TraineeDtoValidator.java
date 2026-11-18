package com.training.gym.util.validation;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

import static com.training.gym.util.validation.ValidationUtils.*;

@Component
@RequiredArgsConstructor
public final class TraineeDtoValidator {

    private final Clock clock;

    public void validateCreate(CreateTraineeDto dto) {
        requireNotNull(dto, "CreateTraineeDto");

        validatePersonalData(
                dto.firstName(),
                dto.lastName(),
                dto.dateOfBirth()
        );
    }

    public void validateUpdate(UpdateTraineeDto dto) {
        requireNotNull(dto, "UpdateTraineeDto");

        requirePositiveId(dto.id(), "Trainee ID");
        requireNotBlank(dto.firstName(), "First name");
        requireNotBlank(dto.lastName(), "Last name");
        requireNotBlank(dto.userName(), "Username");
    }

    private void validatePersonalData(String firstName, String lastName, LocalDate dateOfBirth) {
        requireNotBlank(firstName, "First name");
        requireNotBlank(lastName, "Last name");
        requireValidDateOfBirth(dateOfBirth, LocalDate.now(clock));
    }
}
