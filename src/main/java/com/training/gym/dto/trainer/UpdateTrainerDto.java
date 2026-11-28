package com.training.gym.dto.trainer;


public record UpdateTrainerDto(

         String firstName,
         String lastName,
         String userName,
         Boolean active
) {
}
