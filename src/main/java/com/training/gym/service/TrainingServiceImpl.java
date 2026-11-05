package com.training.gym.service;

import com.training.gym.util.IdGenerator;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Training;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.dao.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final IdGenerator idGenerator;
    private final TrainingMapper trainingMapper;

    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               @Qualifier("trainingIdGenerator")
                               IdGenerator idGenerator,
                               TrainingMapper trainingMapper) {
        this.trainingRepository = trainingRepository;
        this.idGenerator = idGenerator;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public Training createTraining(CreateTrainingDto trainingDto) {
        log.debug("Creating training");
        Objects.requireNonNull(trainingDto, "Create trainer DTO must not be null");

        Training training = trainingMapper.toTraining(trainingDto);

        training.setId(idGenerator.generate());

        Training savedTraining = trainingRepository.save(training);

        log.info("Training created successfully: training id ={}", training.getId());
        return savedTraining;
    }

    @Override
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));
    }

}
