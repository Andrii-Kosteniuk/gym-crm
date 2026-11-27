package com.training.gym.service.auth;

import com.training.gym.domain.model.User;


public interface AuthenticationService {

    User authenticate(String username, String password);


}
