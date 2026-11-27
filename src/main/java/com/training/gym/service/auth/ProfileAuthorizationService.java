package com.training.gym.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("profileAuthorizationService")
@Slf4j
public class ProfileAuthorizationService {

    public boolean isOwner(Authentication authentication, String username) {
        return authentication != null
                && authentication.isAuthenticated()
                && username != null
                && username.equals(authentication.getName());
    }
}