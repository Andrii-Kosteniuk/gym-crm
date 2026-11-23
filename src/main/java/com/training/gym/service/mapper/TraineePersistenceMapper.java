package com.training.gym.service.mapper;


import com.training.gym.domain.model.Address;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.persistence.entity.AddressEntity;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface TraineePersistenceMapper {

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "address", source = "addressEntity")
    @Mapping(target = "trainers", source = "trainerEntities")
    @Mapping(target = "trainings", ignore = true)
    Trainee toTraineeDomain(TraineeEntity entity);

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toTrainerDomain(TrainerEntity entity);

    Set<Trainer> toTrainersDomainList(Set<TrainerEntity> entities);

    @Mapping(target = "userEntity", source = "user")
    @Mapping(target = "addressEntity", source = "address")
    @Mapping(target = "trainerEntities", ignore = true)
    @Mapping(target = "trainingEntities", ignore = true)
    TraineeEntity toEntity(Trainee entity);

    default AddressEntity mapAddress(Address address) {
        if (address == null) {
            return null;
        }

        AddressEntity entity = new AddressEntity();
        entity.setCity(address.getCity());
        entity.setStreet(address.getStreet());
        entity.setBuildingNumber(address.getBuildingNumber());
        entity.setZipCode(address.getZipCode());

        return entity;
    }
}
