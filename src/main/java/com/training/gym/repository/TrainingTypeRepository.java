package com.training.gym.repository;


import com.training.gym.persistence.entity.TrainingTypeEntity;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository {

    Optional<TrainingTypeEntity> findById(Long id);

    List<TrainingTypeEntity> findAll();

}




