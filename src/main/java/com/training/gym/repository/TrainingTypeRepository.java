package com.training.gym.repository;


import com.training.gym.domain.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository {

    Optional<TrainingType> findById(Long id);

    List<TrainingType> findAll();

}




