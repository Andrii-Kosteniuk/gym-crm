package com.training.gym.service.auth;


public interface CredentialService {

    void changePassword(String username, String oldPassword, String newPassword);
    void verifyIfUserExists(String firstName, String lastName);
}
