package com.training.gym.domain.mapper;

import com.training.gym.domain.model.Address;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.AddressResponse;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

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


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", source = "userName")
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toUpdateTrainee(UpdateTraineeDto dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "trainers", source = "trainers")
    TraineeResponseDto toResponseDto(Trainee trainee);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.userName", source = "userName")
    @Mapping(target = "user.active", source = "active")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toTraineeDomain(TraineeResponseDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "type", source = "specialization")
    TrainerResponseDto toResponseDto(Trainer trainer);

    AddressResponse toAddressResponse(Address address);

    TrainingTypeResponseDto toTrainingTypeResponse(TrainingType trainingType);

    List<TrainerResponseDto> toTrainerResponseList(Set<Trainer> trainers);
}
