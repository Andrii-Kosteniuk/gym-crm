package com.training.gym.repository;

import com.training.gym.domain.Trainee;
import java.util.Optional;
import java.util.Set;

public interface TraineeRepository extends CrudRepository<Trainee, Long> {

    Trainee update(Trainee trainee);

    Optional<Trainee> findByUserName(String username);

    void delete(Trainee trainee);

    void deleteByUsername(String username);

    void replaceTrainers(Long traineeId, Set<Long> trainerIds);

    void activateTrainee(Trainee trainee);

    void deactivateTrainee(Trainee traineeEntity);

}
