package com.training.gym.web.controller;

import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.web.dto.request.ChangeLoginRequestDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
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

    @PutMapping("/login-change")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequestDto request,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        Credentials credentials = credentialParser.parse(authorization);

        authenticationService.authenticate(credentials.username(), credentials.password());

        applicationEntry.changePassword(request);

        return ResponseEntity.ok().build();
    }
}
