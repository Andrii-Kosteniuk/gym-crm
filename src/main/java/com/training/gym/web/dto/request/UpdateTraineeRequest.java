package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Editable trainee profile information")
public record UpdateTraineeRequest(

        @NotBlank
        @Schema(description = "Account username; cannot be changed")
        String userName,

        @NotBlank
        @Schema(description = "First name")
        String firstName,

        @NotBlank
        @Schema(description = "Last name")
        String lastName,

        @Past
        @Schema(description = "Date of birth (optional)")
        LocalDate dateOfBirth,

        @Valid
        @Schema(description = "Address (optional)")
        AddressRequest address,

        @NotNull
        @Schema(description = "Activation status")
        Boolean active
) {
}
