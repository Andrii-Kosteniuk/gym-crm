package com.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
@Getter
@Setter
public class TraineeEntity {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private UserEntity userEntity;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Embedded
    private AddressEntity addressEntity;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainee_trainers",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_trainee_trainer",
                    columnNames = {"trainee_id", "trainer_id"}
            )
    )
    private Set<TrainerEntity> trainerEntities = new HashSet<>();

    @OneToMany(mappedBy = "traineeEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<TrainingEntity> trainingEntities = new HashSet<>();
}