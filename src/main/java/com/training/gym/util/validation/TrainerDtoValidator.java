package com.training.gym.util.validation;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

import static com.training.gym.util.validation.ValidationUtils.*;

@Component
@RequiredArgsConstructor
public class TrainerDtoValidator {

    private final Clock clock;

    public void validateCreate(CreateTrainerDto dto) {
        requireNotNull(dto, "CreateTrainerDto");

        validatePersonalData(dto.firstName(), dto.lastName());
    }

    public void validateUpdate(UpdateTrainerDto dto) {
        requireNotNull(dto, "UpdateTrainerDto");
        requireNotBlank(dto.userName(), "Username");

        validatePersonalData(dto.firstName(), dto.lastName());
    }

    private void validatePersonalData(String firstName, String lastName) {
        requireNotBlank(firstName, "First name");
        requireNotBlank(lastName, "Last name");

    }
}