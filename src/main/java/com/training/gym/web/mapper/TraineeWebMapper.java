package com.training.gym.web.mapper;

import com.training.gym.dto.trainee.AddressDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.web.dto.request.AddressRequest;
import com.training.gym.web.dto.request.TraineeRegistrationRequest;
import com.training.gym.web.dto.response.AddressWebResponse;
import com.training.gym.web.dto.response.TraineeProfileResponse;
import com.training.gym.web.dto.response.TrainerSummaryResponse;
import com.training.gym.web.dto.response.TrainingTypeWebResponse;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TraineeWebMapper {

    CreateTraineeDto toCreateDto(TraineeRegistrationRequest request);

    AddressDto toAddressDto(AddressRequest request);

    TraineeProfileResponse toTraineeProfileResponse(TraineeResponseDto responseDto);

    List<TrainerSummaryResponse> toTraineeSummaryResponse(List<TrainerResponseDto> responseDtoList);

    AddressWebResponse toAddressResponse(AddressDto addressDto);

    TrainerSummaryResponse toTrainerSummaryResponse(TrainerResponseDto responseDto);

    TrainingTypeWebResponse toTrainingTypeResponse(TrainingTypeResponseDto typeResponseDto);

}
