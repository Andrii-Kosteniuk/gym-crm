package com.training.gym.service;


import com.training.gym.domain.model.User;

public interface CredentialService {

    User authenticate(String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);
}
