package com.training.gym.repository;

import com.training.gym.domain.User;

import java.util.Optional;

public interface UserRepository {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    void updatePassword(Long userId, String encodedPassword);

    void delete(User user);
}
