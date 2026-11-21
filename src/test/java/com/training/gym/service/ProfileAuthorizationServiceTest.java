//package com.training.gym.service;
//
//import com.training.gym.domain.model.Trainee;
//import com.training.gym.domain.model.Trainer;
//import com.training.gym.domain.model.User;
//import com.training.gym.dto.auth.Credentials;
//import com.training.gym.exception.AccessDeniedException;
//import com.training.gym.exception.AuthenticationFailedException;
//import com.training.gym.persistence.entity.TraineeEntity;
//import com.training.gym.persistence.entity.TrainerEntity;
//import com.training.gym.persistence.entity.UserEntity;
//import com.training.gym.repository.TraineeRepository;
//import com.training.gym.repository.TrainerRepository;
//import com.training.gym.service.auth.CredentialService;
//import com.training.gym.service.auth.ProfileAuthorizationService;
//import com.training.gym.service.mapper.TraineePersistenceMapper;
//import com.training.gym.service.mapper.TrainerPersistenceMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProfileAuthorizationServiceTest {
//    @Mock
//    CredentialService credentialService;
//    @Mock
//    TraineeRepository trainees;
//    @Mock
//    TrainerRepository trainers;
//
//    @Mock
//    TraineePersistenceMapper traineeMapper;
//
//    @Mock
//    TrainerPersistenceMapper trainerMapper;
//
//    @InjectMocks
//    ProfileAuthorizationService service;
//
//    private Credentials jamesCredentials() {
//        return new Credentials("james.bond", "secret");
//    }
//
//    private User james() {
//        return User.builder()
//                .id(1L)
//                .firstName("James")
//                .lastName("Bond")
//                .userName("james.bond")
//                .build();
//    }
//
//    @Test
//    void shouldAllowTraineeToAccessOwnProfile() {
//        // Arrange
//        Long traineeId = 1L;
//
//        User user = james();
//        Trainee traineeDomain = new Trainee();
//        traineeDomain.setId(traineeId);
//        traineeDomain.setUser(user);
//
//        TraineeEntity trainee = new TraineeEntity();
//        UserEntity userEntity = new UserEntity();
//        userEntity.setId(user.getId());
//        trainee.setId(traineeId);
//        trainee.setUserEntity(userEntity);
//        trainee.getUserEntity().setUserName(user.getUserName());
//
//        Credentials credentials = jamesCredentials();
//
//        when(traineeMapper.toTraineeDomain(trainee)).thenReturn(traineeDomain);
//
//        when(credentialService.authenticate("james.bond", "secret"))
//                .thenReturn(user);
//
//        when(trainees.findByUserName("james.bond"))
//                .thenReturn(Optional.of(trainee));
//
//        // Act & Assert
//        assertDoesNotThrow(() -> service.requireTraineeOwner(credentials, traineeId));
//
//        verify(credentialService).authenticate("james.bond", "secret");
//
//        verify(trainees).findByUserName("james.bond");
//    }
//
//    @Test
//    void shouldRejectUserWithoutTraineeProfile() {
//        // Arrange
//        when(credentialService.authenticate("james.bond", "secret")).thenReturn(james());
//        when(trainees.findByUserName("james.bond")).thenReturn(Optional.empty());
//        // Act / Assert
//        assertThrows(AuthenticationFailedException.class, () -> service.requireTraineeOwner(jamesCredentials(), james().getId()));
//    }
//
//    @Test
//    void shouldRejectAccessToOtherTrainee() {
//        // Arrange
//        User user = james();
//        Trainee traineeDomain = new Trainee();
//        traineeDomain.setId(user.getId());
//        traineeDomain.setUser(user);
//
//        TraineeEntity trainee = new TraineeEntity();
//        trainee.setUserEntity(new UserEntity());
//        when(credentialService.authenticate("james.bond", "secret")).thenReturn(user);
//        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(trainee));
//        when(traineeMapper.toTraineeDomain(trainee)).thenReturn(traineeDomain);
//        // Act / Assert
//        assertThrows(AccessDeniedException.class, () -> service.requireTraineeOwner(jamesCredentials(), 999L));
//    }
//
//    @Test
//    void shouldRejectNullCredentialsBeforeAuthentication() {
//        // Arrange
//        Credentials missing = null;
//        // Act / Assert
//        assertThrows(AuthenticationFailedException.class, () -> service.requireTraineeOwner(missing, james().getId()));
//        verifyNoInteractions(credentialService);
//    }
//
//    @Test
//    void shouldAuthenticateTrainer() {
//        // Arrange
//        User sara = User.builder()
//                .userName("sara.connor")
//                .build();
//
//        Trainer trainer = Trainer.builder()
//                .user(sara)
//                .build();
//
//        TrainerEntity trainerEntity = new TrainerEntity();
//        trainerEntity.setId(trainer.getId());
//
//        when(credentialService.authenticate("sara.connor", "secret")).thenReturn(sara);
//        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(trainerEntity));
//        when(trainerMapper.toTrainerDomain(trainerEntity)).thenReturn(trainer);
//        // Act
//        service.requireTrainerOwner(new Credentials("sara.connor", "secret"), sara.getId());
//        // Assert
//        assertDoesNotThrow(() -> new AuthenticationFailedException("Authentication Failed"));
//    }
//
//    @Test
//    void shouldRejectAccessToOtherTrainer() {
//        // Arrange
//        User sara = User.builder()
//                .userName("sara.connor")
//                .build();
//
//        Trainer trainer = Trainer.builder()
//                .user(sara)
//                .build();
//
//        TrainerEntity trainerEntity = new TrainerEntity();
//        trainerEntity.setId(trainer.getId());
//
//        when(trainerMapper.toTrainerDomain(trainerEntity)).thenReturn(trainer);
//        when(credentialService.authenticate("sara.connor", "secret")).thenReturn(sara);
//        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(trainerEntity));
//        // Act / Assert
//        assertThrows(AccessDeniedException.class, () -> service.requireTrainerOwner(new Credentials("sara.connor", "secret"), james().getId()));
//    }
//}
