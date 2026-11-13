package com.training.gym.dto;

public record AddressResponse(
        String city,
        String street,
        int buildNumber,
        String zipcode
) {

}

