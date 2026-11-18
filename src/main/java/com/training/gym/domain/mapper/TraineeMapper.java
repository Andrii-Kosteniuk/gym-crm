package com.training.gym.domain.mapper;

import com.training.gym.domain.Address;
import com.training.gym.domain.Trainee;
import com.training.gym.dto.AddressResponse;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toTrainee(CreateTraineeDto dto);


    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", source = "userName")
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toUpdateTrainee(UpdateTraineeDto dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "active", source = "user.active")
    TraineeResponseDto toResponseDto(Trainee trainee);

    @Mapping(target = "buildNumber", source = "buildingNumber")
    @Mapping(target = "zipcode", source = "zipCode")
    AddressResponse toAddressResponse(Address address);
}
