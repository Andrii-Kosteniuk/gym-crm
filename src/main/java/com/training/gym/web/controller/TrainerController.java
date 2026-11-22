package com.training.gym.web.controller;

import com.training.gym.dto.auth.AuthenticationResponse;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.dto.request.ProfileStatusChangeRequest;
import com.training.gym.web.dto.request.TrainerRegistrationRequest;
import com.training.gym.web.dto.request.UpdateTrainerRequest;
import com.training.gym.web.dto.response.TrainerProfileResponse;
import com.training.gym.web.dto.response.TrainerSummaryResponse;
import com.training.gym.web.dto.response.TrainerTrainingResponse;
import com.training.gym.web.mapper.TrainerWebMapper;
import com.training.gym.web.mapper.TrainingWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialsParser;
    private final TrainerWebMapper trainerWebMapper;
    private final TrainingWebMapper trainingWebMapper;

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {

        var createDto = trainerWebMapper.toCreateDto(request);

        var authenticationResponse = applicationEntry.createTrainer(createDto);

        return ResponseEntity.ok().body(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> authenticate(@Valid @RequestBody Credentials credentials) {
        applicationEntry.authenticate(credentials);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable("username") String username,
                                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        var credentials = credentialsParser.parse(authorization);

        var trainerResponseDto = applicationEntry.getTrainerByUserName(username, credentials);

        var profileResponse = trainerWebMapper.toTrainerProfileResponse(trainerResponseDto);

        return ResponseEntity.ok().body(profileResponse);

    }

    @PutMapping
    public ResponseEntity<TrainerProfileResponse> updateTrainer(@Valid @RequestBody UpdateTrainerRequest request,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        var credentials = credentialsParser.parse(authorization);

        var updateDto = trainerWebMapper.toUpdateDto(request);

        var trainerResponseDto = applicationEntry.updateTrainer(updateDto, credentials);

        var profileResponse = trainerWebMapper.toTrainerProfileResponse(trainerResponseDto);

        return ResponseEntity.ok().body(profileResponse);

    }

    @GetMapping("/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainingsList(@RequestParam(value = "username") String username,

                                                                                 @RequestParam(value = "fromDate", required = false)
                                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                                 LocalDate fromDate,

                                                                                 @RequestParam(value = "toDate", required = false)
                                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                                 LocalDate toDate,

                                                                                 @RequestParam(value = "traineeName", required = false)
                                                                                 String traineeName,

                                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialsParser.parse(authorization);

        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(fromDate, toDate, traineeName);

        var trainerTrainingsResponse = applicationEntry.getTrainerTrainings(username, criteria, credentials);

        List<TrainerTrainingResponse> trainingResponses = trainingWebMapper.toTrainerTrainingResponses(trainerTrainingsResponse);

        return ResponseEntity.ok().body(trainingResponses);

    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> activateDeactivateTrainerProfile(@Valid @RequestBody ProfileStatusChangeRequest request,
                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialsParser.parse(authorization);

        applicationEntry.changeTrainerProfileStatus(request.username(), request.active(), credentials);

        return ResponseEntity.ok().build();

    }

}
