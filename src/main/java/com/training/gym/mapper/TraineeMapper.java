package com.training.gym.mapper;

import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.model.Trainee;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    Trainee toTrainee(CreateTraineeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "password", ignore = true)
    Trainee toUpdateTrainee(UpdateTraineeDto dto);


    TraineeResponseDto toResponseDto(Trainee trainee);
}
