package com.training.gym.dto.trainee;

import jakarta.validation.constraints.Positive;

public record AddressDto(
        String city,
        String street,
        @Positive Integer buildingNumber,
        String zipCode

) {
}
