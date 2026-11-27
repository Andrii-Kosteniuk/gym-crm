package com.training.gym.web.controller;

import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.dto.request.CreateTrainingRequest;
import com.training.gym.web.mapper.TrainingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Training", description = "Gym CRM training endpoints")
@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final GymCrmFacade applicationEntry;
    private final TrainingWebMapper trainingWebMapper;

    @Operation(summary = "Create training",
            description = "Creates a training session for an existing trainee and trainer.",
            operationId = "addTraining")
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

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody CreateTrainingRequest request) {

        NewTrainingDto newTrainingDto = trainingWebMapper.toNewTrainingDto(request);

        applicationEntry.createTraining(newTrainingDto);

        return ResponseEntity.ok().build();

    }
}
