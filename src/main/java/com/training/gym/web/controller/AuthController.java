package com.training.gym.web.controller;

import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.web.dto.request.ChangeLoginRequestDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialParser;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Change user password",
            description = """
                    Changes the password of an authenticated trainee or trainer.
                    The old password must match the current password.
                    Users cannot change another user's password.
                    """
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Password changed successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(responseCode = "401",
                    description = "Invalid username or password"),
            @ApiResponse(responseCode = "403",
                    description = "Cannot change another user's password"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @PutMapping("/login-change")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequestDto request,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialParser.parse(authorization);

        authenticationService.authenticate(credentials.username(), credentials.password());

        applicationEntry.changePassword(request);

        return ResponseEntity.ok().build();
    }
}
