package com.training.gym.web.controller;

import com.training.gym.dto.auth.AuthenticationResponse;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.dto.request.ProfileStatusChangeRequest;
import com.training.gym.web.dto.request.TraineeRegistrationRequest;
import com.training.gym.web.dto.request.UpdateTraineeRequest;
import com.training.gym.web.dto.response.TraineeProfileResponse;
import com.training.gym.web.dto.response.TraineeTrainingResponse;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialParser;
    private final TraineeWebMapper traineeWebMapper;
    private final TrainingWebMapper trainingWebMapper;

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {

        CreateTraineeDto createDto = traineeWebMapper.toCreateDto(request);

        var response = applicationEntry.createTrainee(createDto);

        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<Void> loginTrainee(@Valid @RequestBody Credentials credentials) {
        applicationEntry.authenticate(credentials);
        return ResponseEntity.ok().build();

    }


    @GetMapping("/{username}/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable("username") String username,
                                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        var responseDto = applicationEntry.getTraineeByUserName(username, credentials);

        var traineeProfileResponse = traineeWebMapper.toTraineeProfileResponse(responseDto);

        return ResponseEntity.ok().body(traineeProfileResponse);

    }

    @PutMapping
    public ResponseEntity<TraineeProfileResponse> updateTrainee(@Valid @RequestBody UpdateTraineeRequest request,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        var credentials = credentialParser.parse(authorization);

        var updateTraineeDto = trainingWebMapper.toUpdateTraineeDto(request);

        var traineeResponseDto = applicationEntry.updateTrainee(updateTraineeDto, credentials);

        var traineeProfileResponse = traineeWebMapper.toTraineeProfileResponse(traineeResponseDto);

        return ResponseEntity.ok().body(traineeProfileResponse);

    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable("username") String username,
                                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        applicationEntry.removeTraineeByUsername(username, credentials);

        return ResponseEntity.ok().build();

    }


    @GetMapping("/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainingsList(@RequestParam(value = "username") String username,

                                                                                 @RequestParam(value = "fromDate", required = false)
                                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                                 LocalDate fromDate,

                                                                                 @RequestParam(value = "toDate", required = false)
                                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                                 LocalDate toDate,

                                                                                 @RequestParam(value = "trainerName", required = false)
                                                                                 String trainerName,

                                                                                 @RequestParam(value = "trainingType", required = false)
                                                                                 String trainingType,

                                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialParser.parse(authorization);

        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                username,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );

        var traineeTrainingsResponse = applicationEntry.getTraineeTrainings(username, criteria, credentials);

        var trainingResponses = trainingWebMapper.toTraineeTrainingResponses(traineeTrainingsResponse);

        return ResponseEntity.ok().body(trainingResponses);

    }

    @Operation(
            summary = "Activate or deactivate trainee profile",
            description = """
                    Changes the trainee activation status.
                    Set active=true to activate or active=false to deactivate.
                    Repeating the current status is a business rule violation.
                    """
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value ={
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainee status changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trainee not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Trainee already has the requested status"
            )
    })
    @PatchMapping("/profile")
    public ResponseEntity<Void> activateDeactivateTraineeProfile(@Valid @RequestBody ProfileStatusChangeRequest request,
                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialParser.parse(authorization);

        applicationEntry.changeTraineeProfileStatus(request.username(), request.active(), credentials);

        return ResponseEntity.ok().build();

    }

}
