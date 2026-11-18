package com.training.gym.domain.mapper;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingCriteriaMapper {

    TraineeTrainingFilter toFilter(TraineeTrainingCriteria criteria);

    TrainerTrainingFilter toFilter(TrainerTrainingCriteria criteria);
}