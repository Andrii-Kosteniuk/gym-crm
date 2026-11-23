package com.training.gym.web.controller;

import com.training.gym.dto.auth.AuthenticationResponse;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.dto.request.ProfileStatusChangeRequest;
import com.training.gym.web.dto.request.TrainerRegistrationRequest;
import com.training.gym.web.dto.request.UpdateTrainerRequest;
import com.training.gym.web.dto.response.TrainerProfileResponse;
import com.training.gym.web.dto.response.TrainerTrainingResponse;
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

@Tag(name = "Trainer", description = "Gym CRM trainer endpoints")
@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialsParser;
    private final TrainerWebMapper trainerWebMapper;
    private final TrainingWebMapper trainingWebMapper;

    @Operation(summary = "Register trainer",
            description = "Creates a trainer with the selected specialization and generated credentials.",
            operationId = "registerTrainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
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

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {

        var createDto = trainerWebMapper.toCreateDto(request);

        var authenticationResponse = applicationEntry.createTrainer(createDto);

        return ResponseEntity.ok().body(authenticationResponse);
    }

    @Operation(summary = "Get trainer profile",
            description = "Returns the trainer profile and assigned trainees.",
            operationId = "getTrainerProfile")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerProfileResponse.class))),
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
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable("username") String username,
                                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        var credentials = credentialsParser.parse(authorization);

        var trainerResponseDto = applicationEntry.getTrainerByUserName(username, credentials);

        var profileResponse = trainerWebMapper.toTrainerProfileResponse(trainerResponseDto);

        return ResponseEntity.ok().body(profileResponse);

    }

    @Operation(summary = "Update trainer profile",
            description = "Updates editable trainer fields; specialization cannot be changed.",
            operationId = "updateTrainer")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerProfileResponse.class))),
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
    public ResponseEntity<TrainerProfileResponse> updateTrainer(@Valid @RequestBody UpdateTrainerRequest request,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        var credentials = credentialsParser.parse(authorization);

        var updateDto = trainerWebMapper.toUpdateDto(request);

        var trainerResponseDto = applicationEntry.updateTrainer(updateDto, credentials);

        var profileResponse = trainerWebMapper.toTrainerProfileResponse(trainerResponseDto);

        return ResponseEntity.ok().body(profileResponse);

    }

    @Operation(summary = "Search trainer trainings",
            description = "Filters trainer training history by optional inclusive dates and trainee name.",
            operationId = "getTrainerTrainingsList")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainerTrainingResponse.class)))),
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

    @Operation(summary = "Change trainer activation status",
            description = "Changes activation status; requesting the current status results in a conflict.",
            operationId = "activateDeactivateTrainerProfile")
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
    public ResponseEntity<Void> activateDeactivateTrainerProfile(@Valid @RequestBody ProfileStatusChangeRequest request,
                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialsParser.parse(authorization);

        applicationEntry.changeTrainerProfileStatus(request.username(), request.active(), credentials);

        return ResponseEntity.ok().build();

    }

}
