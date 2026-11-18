package com.training.gym.repository;

import com.training.gym.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUserName(String username);

    void delete(Trainer trainer);

    void deleteByUsername(String username);

    void activateTrainer(Trainer trainer);

    void deactivateTrainer(Trainer trainer);

    List<Trainer> findUnassignedTrainers(String username);
}
