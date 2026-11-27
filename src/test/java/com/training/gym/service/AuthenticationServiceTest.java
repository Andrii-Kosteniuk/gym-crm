package com.training.gym.service;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.User;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.service.auth.ProfileAuthorizationService;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String USERNAME = "sara.connor";
    private static final String OTHER_USERNAME = "other.user";
    private static final String PASSWORD = "secret";
    private static final Long ID = 7L;
    private static final Long OTHER_ID = 8L;

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineePersistenceMapper traineeMapper;
    @Mock
    private TrainerPersistenceMapper trainerMapper;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private ProfileAuthorizationService service;

    private Credentials credentials() {
        return new Credentials(USERNAME, PASSWORD);
    }

    private void authenticatedUser() {
        User user = mock(User.class);
        when(user.getUserName()).thenReturn(USERNAME);
        when(authenticationService.authenticate(USERNAME, PASSWORD)).thenReturn(user);
    }

    private Trainee authenticatedTrainee() {
        authenticatedUser();
        TraineeEntity entity = mock(TraineeEntity.class);
        Trainee trainee = mock(Trainee.class);
        when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity));
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(trainee);
        return trainee;
    }

    private Trainer authenticatedTrainer() {
        authenticatedUser();
        TrainerEntity entity = mock(TrainerEntity.class);
        Trainer trainer = mock(Trainer.class);
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity));
        when(trainerMapper.toTrainerDomain(entity)).thenReturn(trainer);
        return trainer;
    }

    @Test
    void allowsTraineeOwnerById() {
        Trainee trainee = authenticatedTrainee();
        when(trainee.getId()).thenReturn(ID);

        assertDoesNotThrow(() -> service.requireTraineeOwner(credentials(), ID));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTraineeOwnerWithDifferentId() {
        Trainee trainee = authenticatedTrainee();
        when(trainee.getId()).thenReturn(ID);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTraineeOwner(credentials(), OTHER_ID));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTraineeOwnerWithNullRequestedId() {
        Trainee trainee = authenticatedTrainee();
        when(trainee.getId()).thenReturn(ID);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTraineeOwner(credentials(), (Long) null));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void allowsTraineeOwnerByUsername() {
        Trainee trainee = authenticatedTrainee();
        User profileUser = mock(User.class);
        when(trainee.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertDoesNotThrow(() -> service.requireTraineeOwner(credentials(), USERNAME));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTraineeOwnerWithDifferentUsername() {
        Trainee trainee = authenticatedTrainee();
        User profileUser = mock(User.class);
        when(trainee.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTraineeOwner(credentials(), OTHER_USERNAME));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTraineeOwnerWithNullRequestedUsername() {
        Trainee trainee = authenticatedTrainee();
        User profileUser = mock(User.class);
        when(trainee.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTraineeOwner(credentials(), (String) null));

        verify(traineeRepository).findByUserName(USERNAME);
    }

    @Test
    void allowsTrainerOwnerById() {
        Trainer trainer = authenticatedTrainer();
        when(trainer.getId()).thenReturn(ID);

        assertDoesNotThrow(() -> service.requireTrainerOwner(credentials(), ID));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTrainerOwnerWithDifferentId() {
        Trainer trainer = authenticatedTrainer();
        when(trainer.getId()).thenReturn(ID);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTrainerOwner(credentials(), OTHER_ID));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTrainerOwnerWithNullRequestedId() {
        Trainer trainer = authenticatedTrainer();
        when(trainer.getId()).thenReturn(ID);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTrainerOwner(credentials(), (Long) null));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void allowsTrainerOwnerByUsername() {
        Trainer trainer = authenticatedTrainer();
        User profileUser = mock(User.class);
        when(trainer.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertDoesNotThrow(() -> service.requireTrainerOwner(credentials(), USERNAME));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTrainerOwnerWithDifferentUsername() {
        Trainer trainer = authenticatedTrainer();
        User profileUser = mock(User.class);
        when(trainer.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTrainerOwner(credentials(), OTHER_USERNAME));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void deniesTrainerOwnerWithNullRequestedUsername() {
        Trainer trainer = authenticatedTrainer();
        User profileUser = mock(User.class);
        when(trainer.getUser()).thenReturn(profileUser);
        when(profileUser.getUserName()).thenReturn(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> service.requireTrainerOwner(credentials(), (String) null));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void rejectsNullCredentialsForTrainee() {
        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTraineeOwner(null, ID));

        verifyNoInteractions(authenticationService, traineeRepository, traineeMapper);
    }

    @Test
    void rejectsNullCredentialsForTrainer() {
        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTrainerOwner(null, ID));

        verifyNoInteractions(authenticationService, trainerRepository, trainerMapper);
    }

    @Test
    void rejectsNullCredentialUsername() {
        Credentials invalid = new Credentials(null, PASSWORD);

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTraineeOwner(invalid, ID));

        verifyNoInteractions(authenticationService, traineeRepository);
    }

    @Test
    void rejectsBlankCredentialUsername() {
        Credentials invalid = new Credentials("  ", PASSWORD);

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTrainerOwner(invalid, ID));

        verifyNoInteractions(authenticationService, trainerRepository);
    }

    @Test
    void rejectsNullCredentialPassword() {
        Credentials invalid = new Credentials(USERNAME, null);

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTraineeOwner(invalid, USERNAME));

        verifyNoInteractions(authenticationService, traineeRepository);
    }

    @Test
    void rejectsBlankCredentialPassword() {
        Credentials invalid = new Credentials(USERNAME, " \t ");

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTrainerOwner(invalid, USERNAME));

        verifyNoInteractions(authenticationService, trainerRepository);
    }

    @Test
    void propagatesAuthenticationFailureForTrainee() {
        when(authenticationService.authenticate(USERNAME, PASSWORD))
                .thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTraineeOwner(credentials(), ID));

        verifyNoInteractions(traineeRepository, traineeMapper);
    }

    @Test
    void propagatesAuthenticationFailureForTrainer() {
        when(authenticationService.authenticate(USERNAME, PASSWORD))
                .thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTrainerOwner(credentials(), ID));

        verifyNoInteractions(trainerRepository, trainerMapper);
    }

    @Test
    void rejectsAuthenticatedUserWithoutTraineeProfile() {
        authenticatedUser();
        when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTraineeOwner(credentials(), ID));

        verifyNoInteractions(traineeMapper);
    }

    @Test
    void rejectsAuthenticatedUserWithoutTrainerProfile() {
        authenticatedUser();
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> service.requireTrainerOwner(credentials(), ID));

        verifyNoInteractions(trainerMapper);
    }
}
