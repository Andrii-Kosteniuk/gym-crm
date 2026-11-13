package com.training.gym;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingTypeDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.model.Address;
import com.training.gym.model.TrainingType;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;

public class GymCrmApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {

            // Register configuration and scan application components
            context.scan("com.training.gym");

            // Initialize Spring container
            context.refresh();

            // Retrieve Facade bean from Spring IoC container
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            System.out.println("\n========== GYM CRM DEMO ==========\n");

            // Retrieve trainee initialized from JSON
            var trainee = facade.getTraineeById(1L);
            System.out.println("Loaded trainee:\n" + trainee);

            // Retrieve second trainee initialized from JSON
            var trainee2 = facade.getTraineeById(2L);
            System.out.println("Loaded second trainee:\n" + trainee2);

            // Retrieve trainer initialized from JSON
            var trainer = facade.getTrainerById(1L);
            System.out.println("Loaded trainer:\n" + trainer);

            // Retrieve training initialized from JSON
            var training = facade.getTrainingById(1L);
            System.out.println("Loaded training:\n" + training);

            // Creating new trainee
            var createdTrainee = facade.createTrainee(
                    new CreateTraineeDto(
                            "Andrii",
                            "Kosteniuk",
                            LocalDate.of(2026, Month.NOVEMBER, 7),
                            createAddress()
                    )
            );

            System.out.println("Created trainee:\n" + createdTrainee);

            // Updating existed trainee
            var updatedTrainee = facade.updateTrainee(
                    new UpdateTraineeDto(
                            "Andrew",
                            "Kosteniuk",
                            createdTrainee.userName(),
                            LocalDate.of(2026, Month.NOVEMBER, 7),
                            createAddress()
                    )
            );
            System.out.println("Updated trainee:\n" + updatedTrainee);


            // Creating new trainer
            var createdTrainer = facade.createTrainer(
                    new CreateTrainerDto(
                            "James",
                            "Bond",
                            LocalDate.of(2026, Month.NOVEMBER, 7),
                            createAddress(),
                            new TrainingType(100L, "Health")
                    )
            );

            System.out.println("Created trainer:\n" + createdTrainer);

            // Updating existed trainer
            var updatedTrainer = facade.updateTrainer(
                    new UpdateTrainerDto(
                            "James",
                            "SuperPower",
                            createdTrainer.userName(),
                            new TrainingType(100L, "Power")
                    )
            );

            System.out.println("Updated trainer:\n" + updatedTrainer);

            // Creating new training
            var createdTraining = facade.createTraining(
                    new CreateTrainingDto(
                            trainee.id(),
                            trainer.id(),
                            "Health training",
                            new TrainingTypeDto(100L, "Health"),
                            LocalDate.now(Clock.systemDefaultZone()).plusDays(1),
                            Duration.ofMinutes(60)
                    )
            );

            System.out.println("Created training:\n" + createdTraining);


            // Check whether trainee storage contains new trainee.
            var traineeById = facade.getTraineeById(createdTrainee.id());

            System.out.println("Loaded trainee:\n" + traineeById);

            System.out.println("========== DEMO COMPLETED ==========");
        }
    }

    private static Address createAddress() {
        return new Address("Wroclaw", "Main Street", 7, "12-345");
    }
}
