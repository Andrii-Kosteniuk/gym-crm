package com.training.gym;

import com.training.gym.config.PersistenceConfig;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResponseDto;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.AddressDto;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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


            // 7. Registration
            System.out.println("\n========== REGISTRATION ==========");

            RegistrationResponseDto registeredTrainee = facade.createTrainee(
                    new CreateTraineeDto(
                            "James",
                            "Bond",
                            LocalDate.of(1995, 5, 10),
                            null
                    )
            );

            System.out.println("Registered trainee: " + registeredTrainee);

            RegistrationResponseDto registeredTrainer = facade.createTrainer(
                    new CreateTrainerDto(
                            "Sarah",
                            "Connor",
                            1L
                    )
            );

            System.out.println("Registered trainer: " + registeredTrainer);


            // 1. Get profiles by username
            TraineeResponseDto traineeByUserName = facade.getTraineeByUserName(
                    TRAINEE_USERNAME, traineeCredentials);

            TrainerResponseDto trainerByUserName = facade.getTrainerByUserName(
                    TRAINER_USERNAME, trainerCredentials);

            System.out.println("Trainee: " + traineeByUserName);
            System.out.println("Trainer: " + trainerByUserName);

            // 2. Get profiles by ID
            System.out.println("\n========== GET PROFILES BY ID ==========");

            System.out.println("Trainee by ID: " +
                    facade.getTraineeById(traineeByUserName.id(), traineeCredentials));

            System.out.println("Trainer by ID: " +
                    facade.getTrainerById(trainerByUserName.id(), trainerCredentials));

            // 3. Update profiles
            System.out.println("\n========== UPDATE PROFILES ==========");

            TraineeResponseDto updatedTrainee = facade.updateTrainee(
                    new UpdateTraineeDto(
                            traineeByUserName.id(),
                            "Updated Trainee name",
                            "Updated Trainee last name",
                            traineeByUserName.userName(),
                            traineeByUserName.dateOfBirth(),
                            new AddressDto(
                                    "Wroclaw",
                                    "Some street",
                                    1,
                                    "12-345"
                            )

                    ),
                    traineeCredentials
            );

            System.out.println("Updated trainee: " + updatedTrainee);

            TrainerResponseDto updatedTrainer = facade.updateTrainer(
                    new UpdateTrainerDto(
                            "Updated Trainer name",
                            "Updated Trainer last name",
                            trainerByUserName.userName(),
                            trainerByUserName.type().id()
                    ),
                    trainerCredentials
            );

            System.out.println("Updated trainer: " + updatedTrainer);

//             4. Create training
            System.out.println("\n========== TRAINING OPERATIONS ==========");

            TrainingResponseDto createdTraining = facade.createTraining(
                    new CreateTrainingDto(
                            traineeByUserName.id(),
                            trainerByUserName.id(),
                            "Demo session",
                            trainerByUserName.type().id(),
                            LocalDate.now(ZoneId.systemDefault()).plusDays(1),
                            45L
                    ),
                    traineeCredentials
            );

            System.out.println("Created training: " + createdTraining);

            System.out.println("Training by ID: " +
                    facade.getTrainingById(createdTraining.id(), traineeCredentials));

            // 5. Get unassigned trainers
            System.out.println("\n========== UNASSIGNED TRAINERS ==========");

            System.out.println("Unassigned trainers: " +
                    facade.getUnassignedTrainers(
                            traineeByUserName.userName(),
                            traineeCredentials));

            // 6. Activate / deactivate profiles
//            System.out.println("\n========== ACTIVATION ==========");
//
//            if (traineeByUserName.active()) {
//                facade.deActivateTrainee(traineeByUserName.id(), traineeCredentials);
//                System.out.println("Trainee deactivated");
//
//            } else {
//                facade.activateTrainee(traineeByUserName.id(), traineeCredentials);
//                System.out.println("Trainee activated");
//            }
//
//            if (trainerByUserName.active()) {
//                facade.deActivateTrainer(trainerByUserName.id(), trainerCredentials);
//                System.out.println("Trainer deactivated");
//
//            } else {
//                facade.activateTrainer(trainerByUserName.id(), trainerCredentials);
//                System.out.println("Trainer activated");
//            }

//            // 7. Get trainee's/trainer's trainings
//
            System.out.println("\n========== TRAINEE TRAININGS ==========");

            var traineeTrainingCriteria = new TraineeTrainingCriteria(
                    LocalDate.of(2026, 11, 1),
                    LocalDate.now(Clock.systemDefaultZone()).plusDays(3),
                    updatedTrainer.firstName() + " " + updatedTrainer.lastName(),
                    null);


            List<TrainingResponseDto> traineeTrainings = facade.getTraineeTrainings(TRAINEE_USERNAME, traineeTrainingCriteria, traineeCredentials);

            System.out.println("Trainee trainings: " + traineeTrainings);

            System.out.println("\n========== TRAINER TRAININGS ==========");


            var trainerTrainingCriteria = new TrainerTrainingCriteria(
                    LocalDate.of(2026, 11, 1),
                    LocalDate.now(Clock.systemDefaultZone()).plusDays(3),
                    updatedTrainee.firstName() + " " + updatedTrainee.lastName());


            List<TrainingResponseDto> trainerTrainings = facade.getTrainerTrainings(TRAINER_USERNAME, trainerTrainingCriteria, trainerCredentials);

            System.out.println("Trainer trainings: " + trainerTrainings);

            System.out.println("\n========== DEMO COMPLETED ==========");
        }
    }
}