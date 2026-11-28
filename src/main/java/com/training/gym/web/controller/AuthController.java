package com.training.gym.web.controller;

import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.request.ChangeLoginRequestDto;
import com.training.gym.web.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Gym CRM auth endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GymCrmFacade applicationEntry;

    @Operation(summary = "Change password",
            description = "Authenticates the current credentials and changes the account password.",
            operationId = "changeLogin")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful request; empty response body",
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

    @PutMapping("/login-change")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequestDto request) {

        applicationEntry.changePassword(request);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Authenticate user",
            description = """
                    Verifies username and password and issues a JWT.
                    Three failed attempts trigger a five-minute login lock.
                    """,
            operationId = "login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successful request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, validation error, or missing request data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Access denied or invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429",
                    description = "Account temporarily locked",
                    headers = @Header(name = "Retry-After",
                            description = "Seconds until login is allowed",
                            schema = @Schema(type = "integer")),
                    content = @Content(
                            mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        var response = applicationEntry.authenticate(request);

        return ResponseEntity.ok().body(response);

    }

    @Operation(summary = "Log out",
            description = "Revokes the presented access token.",
            operationId = "logout")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Successfully logged out",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Missing, expired, invalid or revoked JWT",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected server error", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {

        applicationEntry.logOut(jwt);

        return ResponseEntity.noContent().build();
    }
}
