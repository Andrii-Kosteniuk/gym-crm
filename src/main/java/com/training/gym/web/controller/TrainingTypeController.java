package com.training.gym.web.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.util.CredentialsParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "TrainingType", description = "Gym CRM trainingtype endpoints")
@RestController
@RequestMapping("/api/v1/specializations")
@RequiredArgsConstructor
public class TrainingTypeController {


    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialsParser;
    private final AuthenticationService authenticationService;

    @Operation(summary = "List specializations",
            description = "Returns available training specializations.",
            operationId = "getTrainingTypes")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingTypeResponseDto.class)))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @GetMapping
    public ResponseEntity<List<TrainingTypeResponseDto>> getTrainingTypes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialsParser.parse(authorization);

        authenticationService.authenticate(credentials.username(), credentials.password());

        var response = applicationEntry.getTrainingTypes();

        return ResponseEntity.ok().body(response);

    }

}
