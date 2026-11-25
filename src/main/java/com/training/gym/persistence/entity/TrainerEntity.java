package com.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter @Setter
public class TrainerEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private UserEntity userEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @ManyToMany(mappedBy = "trainerEntities", fetch = FetchType.LAZY)
    private Set<TraineeEntity> traineeEntities = new HashSet<>();

    @OneToMany(mappedBy = "trainerEntity", fetch = FetchType.LAZY)
    private Set<TrainingEntity> trainingEntities = new HashSet<>();
}
