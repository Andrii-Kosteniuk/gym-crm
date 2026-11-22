package com.training.gym.web.mapper;

import com.training.gym.dto.trainee.AddressDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.web.dto.request.AddressRequest;
import com.training.gym.web.dto.request.CreateTrainingRequest;
import com.training.gym.web.dto.request.UpdateTraineeRequest;
import com.training.gym.web.dto.response.TraineeTrainingResponse;
import com.training.gym.web.dto.response.TrainerTrainingResponse;
import com.training.gym.web.dto.response.TrainingTypeWebResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    NewTrainingDto toNewTrainingDto(CreateTrainingRequest trainingRequest);

    UpdateTraineeDto toUpdateTraineeDto(UpdateTraineeRequest traineeRequest);

    AddressDto toAddressDto(AddressRequest addressRequest);
}
