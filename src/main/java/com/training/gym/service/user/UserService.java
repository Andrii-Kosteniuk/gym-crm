package com.training.gym.service.user;


import com.training.gym.web.dto.request.ChangeLoginRequestDto;

public interface UserService {

    void changeLogin(ChangeLoginRequestDto request);
}
