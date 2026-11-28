package com.training.gym.service.auth;

import com.training.gym.web.dto.request.*;
import com.training.gym.web.dto.response.AuthResponse;
import com.training.gym.web.dto.response.RegistrationResponse;
import org.springframework.security.oauth2.jwt.Jwt;


public interface AuthenticationService {

    AuthResponse authenticate(AuthRequest request);

    RegistrationResponse registerTrainee(TraineeRegistrationRequest request);

    RegistrationResponse registerTrainer(TrainerRegistrationRequest request);

    void logout(Jwt jwt);
}
