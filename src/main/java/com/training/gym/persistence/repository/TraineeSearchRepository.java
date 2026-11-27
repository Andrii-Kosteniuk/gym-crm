package com.training.gym.persistence.repository;

import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.TraineeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TraineeSearchRepository {
    public static final String USERNAME_MUST_NOT_BE_BLANK = "Trainee username must not be blank";

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<TraineeEntity> findByUserName(String username) {
        validateUsername(username);

        return entityManager.createQuery("""
                SELECT DISTINCT t
                FROM TraineeEntity t
                JOIN FETCH t.userEntity u
                LEFT JOIN FETCH t.addressEntity
                LEFT JOIN FETCH t.trainerEntities tr
                LEFT JOIN FETCH tr.userEntity
                LEFT JOIN FETCH tr.specialization
                WHERE u.userName = :username
                """, TraineeEntity.class)
                .setParameter("username", username)
                .getResultList().stream()
                .findFirst();
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IncorrectMethodArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }
    }
}
