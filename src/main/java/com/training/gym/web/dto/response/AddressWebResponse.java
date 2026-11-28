package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Address information")
public record AddressWebResponse(

        String city,
        String street,
        int buildingNumber,
        String zipCode
) {
}
