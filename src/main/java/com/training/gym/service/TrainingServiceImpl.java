package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Training;
import com.training.gym.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final IdGenerator idGenerator;

    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               TraineeRepository traineeRepository,
                               TrainerRepository trainerRepository,
                               @Qualifier("trainingIdGenerator")
                               IdGenerator idGenerator) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Training createTraining(Training training) {
        log.debug("Creating training");
        Objects.requireNonNull(training, "Training can not be null");

        validateTrainingReferences(training);

        training.setId(idGenerator.generate());

        Training savedTraining = trainingRepository.save(training);

        log.info("Training created successfully: training id ={}", training.getId());
        return savedTraining;
    }

    @Override
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);
        Objects.requireNonNull(id, "Id can not be null");
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));
    }

    private void validateTrainingReferences(Training training) {

        Long traineeId = training.getTraineeId();
        if (!traineeRepository.existsById(traineeId)) {
            throw new EntityNotFoundException("Trainee with id %d not found".formatted(traineeId));
        }

        Long trainerId = training.getTrainerId();
        if (!trainerRepository.existsById(trainerId)) {
            throw new EntityNotFoundException("Trainer with id %d not found".formatted(trainerId));
        }
    }

}
