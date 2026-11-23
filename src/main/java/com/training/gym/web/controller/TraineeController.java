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
import com.training.gym.web.dto.request.UpdateTraineeTrainersRequest;
import com.training.gym.web.dto.response.TraineeProfileResponse;
import com.training.gym.web.dto.response.TraineeTrainingResponse;
import com.training.gym.web.dto.response.TrainerSummaryResponse;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainerWebMapper;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainee", description = "Gym CRM trainee endpoints")
@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialParser;
    private final TraineeWebMapper traineeWebMapper;
    private final TrainerWebMapper trainerWebMapper;

    private final TrainingWebMapper trainingWebMapper;

    @Operation(summary = "Register trainee",
            description = "Creates a trainee and returns the generated username and initial password.",
            operationId = "registerTrainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Resource conflict or business rule violation",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {

        CreateTraineeDto createDto = traineeWebMapper.toCreateDto(request);

        var response = applicationEntry.createTrainee(createDto);

        return ResponseEntity.ok().body(response);

    }

    @Operation(summary = "Get trainee profile",
            description = "Returns trainee details and assigned trainers.",
            operationId = "getTraineeProfile")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @GetMapping("/{username}/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable("username") String username,
                                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        var responseDto = applicationEntry.getTraineeByUserName(username, credentials);

        var traineeProfileResponse = traineeWebMapper.toTraineeProfileResponse(responseDto);

        return ResponseEntity.ok().body(traineeProfileResponse);

    }


    @Operation(summary = "Update trainee profile",
            description = "Updates editable trainee profile fields.",
            operationId = "updateTrainee")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Resource conflict or business rule violation",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @PutMapping
    public ResponseEntity<TraineeProfileResponse> updateTrainee(@Valid @RequestBody UpdateTraineeRequest request,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        var credentials = credentialParser.parse(authorization);

        var updateTraineeDto = traineeWebMapper.toUpdateTraineeDto(request);

        var traineeResponseDto = applicationEntry.updateTrainee(updateTraineeDto, credentials);

        var traineeProfileResponse = traineeWebMapper.toTraineeProfileResponse(traineeResponseDto);

        return ResponseEntity.ok().body(traineeProfileResponse);

    }

    @Operation(summary = "Delete trainee profile",
            description = "Deletes the trainee identified by username.",
            operationId = "deleteTrainee")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request; empty response body",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Resource conflict or business rule violation",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable("username") String username,
                                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        applicationEntry.removeTraineeByUsername(username, credentials);

        return ResponseEntity.ok().build();

    }

    @Operation(summary = "List unassigned trainers",
            description = "Returns trainers not assigned to the specified trainee.",
            operationId = "getNotAssignedTrainers")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainerSummaryResponse.class)))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> getNotAssignedTrainers(@PathVariable("username") String username,
                                                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        var trainerResponses = applicationEntry.getUnassignedTrainers(username, credentials);

        var trainersResponse = trainerResponses.stream()
                .map(traineeWebMapper::toTrainerSummaryResponse)
                .toList();

        return ResponseEntity.ok().body(trainersResponse);

    }

    @Operation(summary = "Search trainee trainings",
            description = "Filters trainee training history by optional inclusive dates, trainer name and training type.",
            operationId = "getTraineeTrainingsList")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TraineeTrainingResponse.class)))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

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

    @Operation(summary = "Change trainee activation status",
            description = "Changes activation status",
            operationId = "activateDeactivateTraineeProfile")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request; empty response body",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Requested resource not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Resource conflict or business rule violation",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/profile")
    public ResponseEntity<Void> activateDeactivateTraineeProfile(@Valid @RequestBody ProfileStatusChangeRequest request,
                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialParser.parse(authorization);

        applicationEntry.changeTraineeProfileStatus(request.username(), request.active(), credentials);

        return ResponseEntity.ok().build();

    }


    @Operation(
            summary = "Replace trainee trainers by username",
            description = """
                    Replaces the entire list of trainers assigned to a trainee using trainer usernames.
                    """,
            operationId = "replaceTraineeTrainersByUsername",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Trainer assignments updated successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainerSummaryResponse.class)))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request or trainer usernames",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Authenticated user does not own the trainee profile",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Trainee or trainer not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Trainer assignment violates a business rule",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @PutMapping("/{username}/trainers/updates")
    public ResponseEntity<List<TrainerSummaryResponse>> replaceTraineeTrainersByUsername(@PathVariable("username") String username,
                                                                                         @Valid @RequestBody UpdateTraineeTrainersRequest request,
                                                                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialParser.parse(authorization);

        var trainers = applicationEntry.replaceTraineeTrainersByUsername(username, request.trainerUsernames(), credentials);

        var response = trainerWebMapper.toTrainerSummaryResponses(trainers);

        return ResponseEntity.ok(response);
    }
}