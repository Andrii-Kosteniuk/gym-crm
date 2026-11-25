package com.training.gym.service.types;

import com.training.gym.domain.model.TrainingType;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.repository.TrainingTypeRepository;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingTypePersistenceMapper trainingTypeMapper;


    @Override
    public List<TrainingType> getTrainingTypes() {
        List<TrainingTypeEntity> entityList = trainingTypeRepository.findAll();

        return entityList.stream()
                .map(trainingTypeMapper::toTrainingTypeDomain)
                .toList();
    }
}
