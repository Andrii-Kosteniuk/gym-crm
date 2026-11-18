package com.training.gym.persistence.mapper;


import com.training.gym.persistence.entity.AddressEntity;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.domain.Address;
import com.training.gym.domain.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TraineePersistenceMapper {

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "address", source = "addressEntity")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toTraineeDomain(TraineeEntity entity);

    @Mapping(target = "userEntity", source = "user")
    @Mapping(target = "addressEntity", source = "address")
    @Mapping(target = "trainerEntities", ignore = true)
    @Mapping(target = "trainingEntities", ignore = true)
    TraineeEntity toEntity(Trainee entity);

    AddressEntity toEntity(Address address);
}
