package com.training.gym.dto.trainee;

public record UpdateTraineeDto(

        Long id,
        String firstName,
        String lastName,
        String userName
) {
}
