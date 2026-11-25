package com.training.gym.dto.trainee;

public record AddressResponse(
        String city,
        String street,
        int buildNumber,
        String zipcode
) {

}

