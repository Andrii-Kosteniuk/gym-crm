package com.training.gym.service.auth;

import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.response.AuthResponse;


public interface AuthenticationService {

    AuthResponse authenticate(AuthRequest request);

}
