package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Transactional
    public Training createTraining(Training training) {
        log.debug("Creating training");


        Trainee trainee = traineeRepository.findById(training.getTrainee().getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        Trainer trainer = trainerRepository.findById(training.getTrainer().getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));

        TrainingType type = trainingTypeRepository.findById(training.getTrainingType().getId())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);

        Training savedTraining = trainingRepository.save(training);

        log.info("Training created: id={}", savedTraining.getId());
        return savedTraining;
    }

    @Override
    @Transactional(readOnly = true)
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));
    }
}