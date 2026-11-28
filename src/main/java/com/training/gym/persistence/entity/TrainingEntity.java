package com.training.gym.persistence.entity;

import com.training.gym.util.DurationMinutesConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "trainings", indexes = {
        @Index(name = "idx_training_trainee_date", columnList = "trainee_id, training_date"),
        @Index(name = "idx_training_trainer_date", columnList = "trainer_id, training_date")})
@Getter
@Setter

public class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity traineeEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainerEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingTypeEntity;

    @Column(name = "name", nullable = false)
    private String trainingName;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Convert(converter = DurationMinutesConverter.class)
    @Column(name = "duration_minutes", nullable = false)
    private Duration trainingDuration;
}