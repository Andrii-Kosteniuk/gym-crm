package com.training.gym.web.mapper;

import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.web.dto.request.TrainerRegistrationRequest;
import com.training.gym.web.dto.request.UpdateTrainerRequest;
import com.training.gym.web.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerWebMapper {


    TrainerSummaryResponse toTrainerSummaryResponse(TrainerResponseDto trainerResponseDto);

    TrainingTypeWebResponse toTrainingTypeResponse(TrainingTypeResponseDto trainingTypeResponseDto);

    CreateTrainerDto toCreateDto(TrainerRegistrationRequest trainerRegistrationRequest);

    TrainerProfileResponse toTrainerProfileResponse(TrainerResponseDto trainerResponseDto);

    List<TraineeSummaryResponse > toTraineeSummaryResponse(List<TraineeResponseDto> traineeResponses);

    @Mapping(target = "userName", source = "userName" )
    TraineeSummaryResponse  toTraineeSummaryResponse(TraineeResponseDto traineeResponseDto);

    UpdateTrainerDto toUpdateDto(UpdateTrainerRequest updateTrainerRequest);

    List<TrainerSummaryResponse> toTrainerSummaryResponses(List<TrainerResponseDto> trainers);
}
