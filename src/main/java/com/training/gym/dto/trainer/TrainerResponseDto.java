package com.training.gym.dto.trainer;

import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.model.Address;

public record TrainerResponseDto(

        Long id,
        String firstName,
        String lastName,
        String userName,
        Address address,
        boolean active,
        TrainingTypeResponseDto type
) {
}
