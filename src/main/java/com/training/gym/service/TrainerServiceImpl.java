package com.training.gym.service;

import com.training.gym.util.IdGenerator;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.dao.TraineeRepository;
import com.training.gym.model.Trainer;
import com.training.gym.dao.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final IdGenerator idGenerator;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerServiceImpl(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            @Qualifier("trainerIdGenerator") IdGenerator idGenerator,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator
    ) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.idGenerator = idGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer createTrainer(Trainer trainer) {
        log.debug("Creating trainer profile");
        Objects.requireNonNull(trainer, "Trainer can not be null");

        String uniqUserName = usernameGenerator.generate(
                trainer.getFirstName(),
                trainer.getLastName(),
                this::usernameExists);

        trainer.setId(idGenerator.generate());
        trainer.setPassword(passwordGenerator.generate());
        trainer.setUserName(uniqUserName);
        trainer.setActive(true);

        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile created successfully: username={}", uniqUserName);
        return savedTrainer;
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        log.debug("Updating trainer profile");
        Objects.requireNonNull(trainer, "Trainer can not be null");

        String username = trainer.getUserName();

        log.debug("Updating trainer profile: username={}", username);

        Trainer existingTrainer = trainerRepository
                .findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainer update failed: username={} not found", username);
                    return new EntityNotFoundException("Trainer with username %s not found".formatted(username));
                });

        trainer.setId(existingTrainer.getId());
        trainer.setActive(existingTrainer.isActive());

        Trainer updatedTrainer = trainerRepository.update(trainer);

        log.info("Trainer profile updated successfully: username={}", username);
        return updatedTrainer;
    }

    @Override
    public Trainer getTrainerById(Long id) {
        log.debug("Retrieving trainer profile: id={}", id);
        Objects.requireNonNull(id, "Id can not be null");
        return trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with id %d not found".formatted(id)));
    }

    private boolean usernameExists(String username) {
        return traineeRepository.existsByUserName(username)
                || trainerRepository.existsByUserName(username);
    }
}
