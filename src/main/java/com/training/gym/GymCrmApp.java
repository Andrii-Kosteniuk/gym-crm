package com.training.gym;

import com.training.gym.config.PersistenceConfig;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResponseDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.facade.GymCrmFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;

public final class GymCrmApp {

    private static final String DEMO_PASSWORD = "DemoPass123!";
    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String TRAINER_USERNAME = "sarah.connor";

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(PersistenceConfig.class);
            context.refresh();

            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            Credentials traineeCredentials =
                    new Credentials(TRAINEE_USERNAME, DEMO_PASSWORD);

            Credentials trainerCredentials =
                    new Credentials(TRAINER_USERNAME, DEMO_PASSWORD);

            System.out.println("\n========== GYM CRM DEMO ==========");


            // 7. Registration
            System.out.println("\n========== REGISTRATION ==========");

            RegistrationResponseDto registeredTrainee = facade.createTrainee(
                    new CreateTraineeDto(
                            "Some",
                            "Name",
                            LocalDate.of(1995, 5, 10),
                            null
                    )
            );

            System.out.println("Registered trainee: " + registeredTrainee);

            RegistrationResponseDto registeredTrainer = facade.createTrainer(
                    new CreateTrainerDto(
                            "Alice",
                            "Demo",
                            1L
                    )
            );

            System.out.println("Registered trainer: " + registeredTrainer);

            // 1. Get profiles by username
            TraineeResponseDto trainee = facade.getTraineeByUserName(
                    TRAINEE_USERNAME, traineeCredentials);

            TrainerResponseDto trainer = facade.getTrainerByUserName(
                    TRAINER_USERNAME, trainerCredentials);

            System.out.println("Trainee: " + trainee);
            System.out.println("Trainer: " + trainer);

            // 2. Get profiles by ID
            System.out.println("\n========== GET PROFILES BY ID ==========");

            System.out.println("Trainee by ID: " +
                    facade.getTraineeById(trainee.id(), traineeCredentials));

            System.out.println("Trainer by ID: " +
                    facade.getTrainerById(trainer.id(), trainerCredentials));

            // 3. Update profiles
            System.out.println("\n========== UPDATE PROFILES ==========");

            TraineeResponseDto updatedTrainee = facade.updateTrainee(
                    new UpdateTraineeDto(
                            trainee.firstName(),
                            trainee.lastName(),
                            trainee.userName(),
                            trainee.dateOfBirth(),
                            null
                    ),
                    traineeCredentials
            );

            System.out.println("Updated trainee: " + updatedTrainee);

            TrainerResponseDto updatedTrainer = facade.updateTrainer(
                    new UpdateTrainerDto(
                            trainer.firstName(),
                            trainer.lastName(),
                            trainer.userName(),
                            trainer.type().id()
                    ),
                    trainerCredentials
            );

            System.out.println("Updated trainer: " + updatedTrainer);

            // 4. Create training
            System.out.println("\n========== TRAINING OPERATIONS ==========");

            TrainingResponseDto createdTraining = facade.createTraining(
                    new CreateTrainingDto(
                            trainee.id(),
                            trainer.id(),
                            "Demo session",
                            trainer.type().id(),
                            LocalDate.now(ZoneId.systemDefault()).plusDays(1),
                            45L
                    )
            );

            System.out.println("Created training: " + createdTraining);

            System.out.println("Training by ID: " +
                    facade.getTrainingById(createdTraining.id()));

            // 5. Get unassigned trainers
            System.out.println("\n========== UNASSIGNED TRAINERS ==========");

            System.out.println("Unassigned trainers: " + facade.getUnassignedTrainers(TRAINER_USERNAME, trainerCredentials));

            // 6. Activate / deactivate profiles
            System.out.println("\n========== ACTIVATION ==========");

            if (trainee.active()) {
                facade.deActivateTrainee(trainee.id(), traineeCredentials);
                System.out.println("Trainee deactivated");

            } else {
                facade.activateTrainee(trainee.id(), traineeCredentials);
                System.out.println("Trainee activated");
            }

            if (trainer.active()) {
                facade.deActivateTrainer(trainer.id(), trainerCredentials);
                System.out.println("Trainer deactivated");

            } else {
                facade.activateTrainer(trainer.id(), trainerCredentials);
                System.out.println("Trainer activated");
            }

            System.out.println("\n========== DEMO COMPLETED ==========");
        }
    }
}