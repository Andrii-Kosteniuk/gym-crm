package com.training.gym.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AddressRequest(

        @NotBlank(message = "City name should not be empty")
        String city,

        @NotBlank(message = "Street name should not be empty")
        String street,

        @Positive(message = "Number must be a positive value")
        @JsonProperty("number")
        int buildingNumber,

        @NotBlank(message = "Zipcode should not be empty")
        String zipCode
) {
}
