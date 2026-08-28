package com.musicwall.service;

import com.musicwall.dto.ChangePasswordRequest;
import com.musicwall.entity.UserEntity;
import com.musicwall.exception.BusinessException;
import com.musicwall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private ProfileService service;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        service = new ProfileService(userRepository, passwordEncoder);
        user = new UserEntity();
        user.setUsername("janna");
        user.setPassword("stored-hash");
        when(userRepository.findByUsername("janna")).thenReturn(Optional.of(user));
    }

    @Test
    void changesPasswordWhenCurrentPasswordMatches() {
        ChangePasswordRequest request = request("current-password", "new-password");
        when(passwordEncoder.matches("current-password", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword("janna", request);

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        ChangePasswordRequest request = request("wrong-password", "new-password");
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.changePassword("janna", request)
        );

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, never()).encode("new-password");
        verify(userRepository, never()).save(user);
    }

    private ChangePasswordRequest request(String currentPassword, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        return request;
    }
}
