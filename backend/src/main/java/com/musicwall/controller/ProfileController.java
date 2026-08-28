package com.musicwall.controller;

import com.musicwall.dto.ChangePasswordRequest;
import com.musicwall.dto.ProfileAvatarDTO;
import com.musicwall.dto.ProfileDTO;
import com.musicwall.dto.UpdateProfileDTO;
import com.musicwall.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public ProfileDTO getProfile(@PathVariable String username) {
        return profileService.getProfile(username);
    }

    @PutMapping("/me")
    public ProfileDTO updateProfile(
            @Valid @RequestBody UpdateProfileDTO request,
            Authentication authentication
    ) {
        return profileService.updateProfile(authentication.getName(), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        profileService.changePassword(authentication.getName(), request);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDTO uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return profileService.updateAvatar(authentication.getName(), file);
    }

    @GetMapping("/{username}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        ProfileAvatarDTO avatar = profileService.getAvatar(username);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .body(avatar.getImage());
    }
}
