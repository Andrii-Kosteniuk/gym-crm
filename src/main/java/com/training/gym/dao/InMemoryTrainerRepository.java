package com.training.gym.dao;

import com.training.gym.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryTrainerRepository implements TrainerRepository {
    private static final String NOT_NULL_ID_MESSAGE = "Trainer ID must not be null";
    private static final String NOT_BLANK_USERNAME_MESSAGE = "Trainer username must not be empty";
    private Map<Long, Trainer> storage;

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public Trainer save(Trainer entity) {
        Objects.requireNonNull(entity, "Trainer must not be null");
        Long id = Objects.requireNonNull(entity.getId(), NOT_NULL_ID_MESSAGE);

        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        storage.remove(id);
    }

    @Override
    public boolean existsByUserName(String userName) {
        Objects.requireNonNull(userName, NOT_NULL_ID_MESSAGE);

        if (userName.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }

        return storage.values()
                .stream()
                .anyMatch(entity -> entity.getUserName().equals(userName));
    }

    @Override
    public Optional<Trainer> findByUserName(String userName) {
        Objects.requireNonNull(userName, NOT_NULL_ID_MESSAGE);

        if (userName.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }

        return storage.values()
                .stream()
                .filter(entity -> entity.getUserName().equals(userName))
                .findFirst();
    }
}
