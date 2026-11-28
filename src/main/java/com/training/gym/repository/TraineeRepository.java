package com.training.gym.repository;

import com.training.gym.persistence.entity.TraineeEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TraineeRepository {

    TraineeEntity save(TraineeEntity trainee);

    TraineeEntity update(TraineeEntity trainee);

    Optional<TraineeEntity> findById(Long id);

    Optional<TraineeEntity> findByUserName(String username);

    List<TraineeEntity> findAll();

    boolean existsById(Long id);

    void delete(TraineeEntity trainee);

    void deleteByUsername(String username);

    void replaceTrainers(String username, Set<Long> trainerIds);

    void changeProfileStatus(TraineeEntity trainee, boolean active);

}
