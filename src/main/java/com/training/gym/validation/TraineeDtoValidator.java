package com.training.gym.validation;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

import static com.training.gym.validation.ValidationUtils.*;

@Component
@RequiredArgsConstructor
public class TraineeDtoValidator {

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

        requireNotBlank(dto.userName(), "Username");

        validatePersonalData(
                dto.firstName(),
                dto.lastName(),
                dto.dateOfBirth()
        );
    }

    private void validatePersonalData(String firstName, String lastName, LocalDate dateOfBirth) {
        requireNotBlank(firstName, "First name");
        requireNotBlank(lastName, "Last name");

        requireValidDateOfBirth(dateOfBirth, LocalDate.now(clock));
    }
}
