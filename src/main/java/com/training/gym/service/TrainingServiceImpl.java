package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Training;
import com.training.gym.util.IdGenerator;
import com.training.gym.validation.TrainingDtoValidator;
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
    private final TrainingMapper trainingMapper;
    private final TrainingDtoValidator trainingValidator;

    public TrainingServiceImpl(TrainingRepository trainingRepository, TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                               @Qualifier("trainingIdGenerator")
                               IdGenerator idGenerator,
                               TrainingMapper trainingMapper, TrainingDtoValidator trainingValidator) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.idGenerator = idGenerator;
        this.trainingMapper = trainingMapper;
        this.trainingValidator = trainingValidator;
    }

    @Override
    public Training createTraining(CreateTrainingDto trainingDto) {
        log.debug("Creating training");
        Objects.requireNonNull(trainingDto, "Dto can not be null");

        trainingValidator.validateCreate(trainingDto);
        validateTrainingReferences(trainingDto);

        Training training = trainingMapper.toTraining(trainingDto);

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

    private void validateTrainingReferences(CreateTrainingDto dto) {
        if (!traineeRepository.existsById(dto.traineeId())) {
            throw new EntityNotFoundException("Trainee with id %d not found".formatted(dto.traineeId()));
        }

        if (!trainerRepository.existsById(dto.trainerId())) {
            throw new EntityNotFoundException("Trainer with id %d not found".formatted(dto.trainerId()));
        }
    }

}
