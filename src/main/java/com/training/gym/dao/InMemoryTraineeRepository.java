package com.training.gym.dao;

import com.training.gym.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryTraineeRepository implements TraineeRepository {
    private static final String NOT_NULL_ID_MESSAGE = "Trainee ID must not be null";
    private static final String NOT_BLANK_USERNAME_MESSAGE = "Trainee username must not be empty";

    private Map<Long, Trainee> storage;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public Trainee save(Trainee entity) {
        Objects.requireNonNull(entity, "Trainee must not be null");

        Long id = Objects.requireNonNull(entity.getId(), NOT_NULL_ID_MESSAGE);

        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
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
    public Optional<Trainee> findByUserName(String userName) {
        Objects.requireNonNull(userName, NOT_NULL_ID_MESSAGE);

        if (userName.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }
        return storage.values()
                .stream()
                .filter(trainee -> trainee.getUserName().equals(userName))
                .findFirst();
    }
}
