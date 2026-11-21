package com.training.gym.web.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.util.CredentialsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specializations")
@RequiredArgsConstructor
public class TrainingTypeController {


    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialsParser;
    private final AuthenticationService authenticationService;


    @GetMapping
    public ResponseEntity<List<TrainingTypeResponseDto>> getTrainingTypes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialsParser.parse(authorization);

        authenticationService.authenticate(credentials.username(), credentials.password());

        var response = applicationEntry.getTrainingTypes();

        return ResponseEntity.ok().body(response);

    }

}
