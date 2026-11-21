package com.training.gym.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.training.gym.dto.AddressResponse;

import java.time.LocalDate;
import java.util.List;

public record TraineeProfileResponse(


        String firstName,
        String lastName,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate dateOfBirth,
        AddressResponse address,
        boolean active,
        List<TrainerSummaryResponse> trainers
) {
}
