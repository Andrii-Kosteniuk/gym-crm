package com.training.gym.dto.trainee;

public record AddressDto(
        String city,
        String street,
        int buildingNumber,
        String zipCode

) {
}
