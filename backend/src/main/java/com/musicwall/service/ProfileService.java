package com.musicwall.service;

import com.musicwall.dto.ChangePasswordRequest;
import com.musicwall.dto.ProfileAvatarDTO;
import com.musicwall.dto.ProfileDTO;
import com.musicwall.dto.UpdateProfileDTO;
import com.musicwall.entity.UserEntity;
import com.musicwall.exception.BusinessException;
import com.musicwall.exception.ResourceNotFoundException;
import com.musicwall.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    private static final Set<String> AVATAR_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(String username) {
        return convertToDTO(findUser(username));
    }

    @Transactional
    public ProfileDTO updateProfile(String username, UpdateProfileDTO request) {
        UserEntity user = findUser(username);
        user.setBio(cleanOptional(request.getBio()));
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = findUser(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public ProfileDTO updateAvatar(String username, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Choose an image first");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("Avatar image must be smaller than 2 MB");
        }
        if (!AVATAR_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Avatar must be a JPG, PNG or WebP image");
        }

        UserEntity user = findUser(username);
        try {
            user.setAvatarImage(file.getBytes());
        } catch (IOException exception) {
            throw new BusinessException("Could not read avatar image");
        }
        user.setAvatarContentType(file.getContentType());
        return convertToDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public ProfileAvatarDTO getAvatar(String username) {
        UserEntity user = findUser(username);
        if (user.getAvatarImage() == null) {
            throw new ResourceNotFoundException("Avatar not found");
        }
        return new ProfileAvatarDTO(user.getAvatarImage(), user.getAvatarContentType());
    }

    private ProfileDTO convertToDTO(UserEntity user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarImage() == null
                ? null
                : "/api/profiles/" + user.getUsername() + "/avatar");
        return dto;
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String cleanOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
