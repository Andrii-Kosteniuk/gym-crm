package com.training.gym.repository;

import com.training.gym.persistence.entity.UserEntity;

import java.util.Optional;

public interface UserRepository {

    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    void updatePassword(Long userId, String encodedPassword);

    void delete(UserEntity user);
}
