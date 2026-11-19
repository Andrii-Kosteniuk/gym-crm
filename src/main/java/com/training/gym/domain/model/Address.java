package com.training.gym.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Address {

    private String city;
    private String street;
    private int buildingNumber;
    private String zipCode;

    public void setBuildingNumber(int buildingNumber) {
        if (buildingNumber <= 0) {
            throw new IllegalArgumentException("Building number must be greater than 0");
        }

        this.buildingNumber = buildingNumber;
    }
}
