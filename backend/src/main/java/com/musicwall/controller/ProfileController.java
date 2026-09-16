package com.musicwall.controller;

import com.musicwall.dto.ChangePasswordRequest;
import com.musicwall.dto.ProfileDTO;
import com.musicwall.service.AvatarData;
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

// The controller reads profiles and delegates edits to ProfileService.
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
            @Valid @RequestBody ProfileDTO request,
            Authentication authentication
    ) {
        // Use the logged-in account for /me, regardless of fields sent in the request.
        return profileService.updateProfile(authentication.getName(), request);
    }

    // Validate the passwords before the service checks the current password.
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        profileService.changePassword(authentication.getName(), request);
    }

    // Receive the uploaded file with multipart form data; the service checks and stores it.
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDTO uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return profileService.updateAvatar(authentication.getName(), file);
    }

    // Send image bytes with the stored file type, rather than a JSON response.
    @GetMapping("/{username}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        AvatarData avatar = profileService.getAvatar(username);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .body(avatar.getImage());
    }
}
