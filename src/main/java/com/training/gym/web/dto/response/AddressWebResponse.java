package com.training.gym.web.dto.response;

public record AddressWebResponse(

        String city,
        String street,
        int buildingNumber,
        String zipCode
) {
}
