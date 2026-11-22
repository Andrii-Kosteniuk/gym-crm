package com.training.gym.web.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.dto.request.CreateTrainingRequest;
import com.training.gym.web.mapper.TrainingWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final GymCrmFacade applicationEntry;
    private final CredentialsParser credentialsParser;
    private final TrainingWebMapper trainingWebMapper;

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody CreateTrainingRequest request,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Credentials credentials = credentialsParser.parse(authorization);

        NewTrainingDto newTrainingDto = trainingWebMapper.toNewTrainingDto(request);


        applicationEntry.createTraining(newTrainingDto, credentials);

        return ResponseEntity.ok().build();

    }
}
