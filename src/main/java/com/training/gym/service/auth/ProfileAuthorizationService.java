package com.training.gym.service.auth;

import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("profileAuthorizationService")
@RequiredArgsConstructor
@Slf4j
public class ProfileAuthorizationService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TraineePersistenceMapper traineeMapper;
    private final TrainerPersistenceMapper trainerMapper;

    public boolean isOwner(Authentication authentication, String username) {
        return authentication != null
                && authentication.isAuthenticated()
                && username != null
                && username.equals(authentication.getName());
    }

    public boolean isTraineeOwner(Authentication authentication, Long id) {

        if (id == null) {
            return false;
        }

        return traineeRepository.findById(id)
                .map(traineeMapper::toTraineeDomain)
                .map(trainee -> isOwner(
                        authentication,
                        trainee.getUser().getUserName()
                ))
                .orElse(false);
    }

    public boolean isTrainerOwner(Authentication authentication, Long id) {

        if (id == null) {
            return false;
        }

        return trainerRepository.findById(id)
                .map(trainerMapper::toTrainerDomain)
                .map(trainer -> isOwner(
                        authentication,
                        trainer.getUser().getUserName()
                ))
                .orElse(false);
    }
}