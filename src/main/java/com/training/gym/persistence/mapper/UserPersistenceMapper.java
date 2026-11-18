package com.training.gym.persistence.mapper;

import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toUserDomain(UserEntity entity);

}
