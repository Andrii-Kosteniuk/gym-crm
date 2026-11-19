package com.training.gym.service.mapper;


import com.training.gym.domain.model.Address;
import com.training.gym.domain.model.Trainee;
import com.training.gym.persistence.entity.AddressEntity;
import com.training.gym.persistence.entity.TraineeEntity;
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

    default AddressEntity mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCity(address.getCity());
        addressEntity.setStreet(address.getStreet());
        addressEntity.setBuildingNumber(address.getBuildingNumber());
        addressEntity.setZipCode(address.getZipCode());

        return addressEntity;
    }
}
