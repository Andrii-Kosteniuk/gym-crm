package com.training.gym.persistence.repository;

import com.training.gym.domain.User;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.persistence.mapper.UserPersistenceMapper;
import com.training.gym.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    public static final String USERNAME_MUST_NOT_BE_BLANK = "Username must not be blank";

    @PersistenceContext
    private EntityManager entityManager;

    private final UserPersistenceMapper userMapper;

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }

        Long count = entityManager.createQuery("""
                        SELECT COUNT(u)
                        FROM UserEntity u
                        WHERE u.userName = :username
                        """, Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }

        return entityManager.createQuery("""
                        SELECT u
                        FROM UserEntity u
                        WHERE u.userName = :username
                        """, UserEntity.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .map(userMapper::toUserDomain)
                .findFirst();
    }

    @Override
    public void updatePassword(Long userId, String encodedPassword) {
        Objects.requireNonNull(userId, "User ID is required");

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Encoded password is required");
        }

        UserEntity entity = entityManager.find(UserEntity.class, userId);

        if (entity == null) {
            throw new EntityNotFoundException("User not found: " + userId);
        }

        entity.setPassword(encodedPassword);
    }

    @Override
    public void delete(User user) {
        Objects.requireNonNull(user, "User must not be null");

        Long id = Objects.requireNonNull(user.getId(), "User ID must not be null");

        UserEntity managed = entityManager.find(UserEntity.class, id);

        if (managed == null) {
            throw new EntityNotFoundException("User not found: " + id);
        }

        entityManager.remove(managed);
    }
}
