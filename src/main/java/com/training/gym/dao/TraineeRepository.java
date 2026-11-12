package com.training.gym.dao;

import com.training.gym.model.Trainee;

import java.util.Optional;

public interface TraineeRepository extends CrudRepository<Trainee, Long> {

    Optional<Trainee> findByUserName(String username);

    Trainee update(Trainee entity);

    boolean existsByUserName(String userName);
}
