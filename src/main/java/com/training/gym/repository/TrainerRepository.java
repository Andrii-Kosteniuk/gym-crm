package com.training.gym.repository;

import com.training.gym.persistence.entity.TrainerEntity;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository {

    TrainerEntity save(TrainerEntity trainerEntity);

    TrainerEntity update(TrainerEntity trainerEntity);

    Optional<TrainerEntity> findById(Long id);

    Optional<TrainerEntity> findByUserName(String username);

    void delete(TrainerEntity trainerEntity);

    void deleteByUsername(String username);

    void activateTrainer(TrainerEntity trainerEntity);

    void deactivateTrainer(TrainerEntity trainerEntity);

    List<TrainerEntity> findAll();

    boolean existsById(Long id);

    List<TrainerEntity> findUnassignedTrainers(String username);
}
