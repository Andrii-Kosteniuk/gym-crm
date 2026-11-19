package com.training.gym.dto;

public record AddressResponse(
        String city,
        String street,
        int buildingNumber,
        String zipCode
) {

}

