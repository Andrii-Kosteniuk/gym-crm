package com.training.gym.dao;

import com.training.gym.model.Trainer;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    boolean existsByUserName(String userName);

    Optional<Trainer> findByUserName(String username);
}


