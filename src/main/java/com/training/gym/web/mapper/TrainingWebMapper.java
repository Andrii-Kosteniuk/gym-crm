package com.training.gym.web.mapper;

import com.training.gym.domain.model.Training;
import com.training.gym.dto.trainee.AddressDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.web.dto.request.AddressRequest;
import com.training.gym.web.dto.request.CreateTrainingRequest;
import com.training.gym.web.dto.response.TraineeTrainingResponse;
import com.training.gym.web.dto.response.TrainerTrainingResponse;
import com.training.gym.web.dto.response.TrainingTypeWebResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingWebMapper {

    @Mapping(target = "trainerName", source = "trainer.user.firstName")
    TraineeTrainingResponse toTraineeTrainingsResponse(TrainingResponseDto dto);

    @Mapping(target = "traineeName", source = "trainee.user.firstName")
    @Mapping(target = "trainingType", source = "type")
    TrainerTrainingResponse toTrainerTrainingsResponse(TrainingResponseDto dto);

    TrainingTypeWebResponse toResponse(TrainingTypeResponseDto dto);

    List<TraineeTrainingResponse> toTraineeTrainingResponses(List<TrainingResponseDto> responseDtoList);

    List<TrainerTrainingResponse> toTrainerTrainingResponses(List<TrainingResponseDto> responseDtoList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainee.user.userName", source = "traineeUsername")
    @Mapping(target = "trainer.user.userName", source = "trainerUsername")
    @Mapping(target = "trainingType", ignore = true)
    Training toTrainingDomain(CreateTrainingRequest trainingRequest);

    AddressDto toAddressDto(AddressRequest addressRequest);

    default Duration toDuration(Long duration){
        return duration == null
                ? null
                : Duration.ofMinutes(duration);
    }

}
