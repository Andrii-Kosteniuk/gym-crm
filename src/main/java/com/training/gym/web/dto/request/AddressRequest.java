package com.training.gym.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Address supplied during trainee registration or update")
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
