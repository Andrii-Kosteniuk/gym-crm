package com.training.gym.dto.trainer;

import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;

import java.util.List;


public record TrainerResponseDto(

        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean active,
        TrainingTypeResponseDto type,
        List<TraineeResponseDto> trainees
) {
}
