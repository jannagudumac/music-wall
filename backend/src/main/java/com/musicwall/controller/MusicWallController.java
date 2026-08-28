package com.musicwall.controller;

import com.musicwall.dto.AddWallMemberRequest;
import com.musicwall.dto.CreateMusicWallRequest;
import com.musicwall.dto.MusicWallDTO;
import com.musicwall.dto.MusicWallDetailDTO;
import com.musicwall.dto.UpdateWallAppearanceRequest;
import com.musicwall.dto.UserSearchDTO;
import com.musicwall.dto.WallMemberDTO;
import com.musicwall.service.MusicWallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/walls")
public class MusicWallController {

    private final MusicWallService musicWallService;

    public MusicWallController(MusicWallService musicWallService) {
        this.musicWallService = musicWallService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MusicWallDTO createWall(
            Authentication authentication,
            @Valid @RequestBody CreateMusicWallRequest request
    ) {
        return musicWallService.createWall(authentication.getName(), request);
    }

    @GetMapping
    public List<MusicWallDTO> getMyWalls(Authentication authentication) {
        return musicWallService.getWallsForUser(authentication.getName());
    }

    @GetMapping("/{id}")
    public MusicWallDetailDTO getWall(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return musicWallService.getWall(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public MusicWallDTO updateWall(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody CreateMusicWallRequest request
    ) {
        return musicWallService.updateWall(id, authentication.getName(), request);
    }

    @PutMapping("/{id}/appearance")
    public MusicWallDTO updateWallAppearance(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateWallAppearanceRequest request
    ) {
        return musicWallService.updateWallAppearance(
                id, authentication.getName(), request
        );
    }

    @GetMapping("/{wallId}/members")
    public List<WallMemberDTO> getMembers(
            @PathVariable Long wallId,
            Authentication authentication
    ) {
        return musicWallService.getMembers(wallId, authentication.getName());
    }

    @GetMapping("/{wallId}/members/search")
    public List<UserSearchDTO> searchMemberCandidates(
            @PathVariable Long wallId,
            @RequestParam String query,
            Authentication authentication
    ) {
        return musicWallService.searchMemberCandidates(
                wallId, authentication.getName(), query
        );
    }

    @PostMapping("/{wallId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public WallMemberDTO addMember(
            @PathVariable Long wallId,
            Authentication authentication,
            @Valid @RequestBody AddWallMemberRequest request
    ) {
        return musicWallService.addMember(
                wallId, authentication.getName(), request
        );
    }

    @DeleteMapping("/{wallId}/members/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable Long wallId,
            @PathVariable String username,
            Authentication authentication
    ) {
        musicWallService.removeMember(wallId, authentication.getName(), username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWall(
            @PathVariable Long id,
            Authentication authentication
    ) {
        musicWallService.deleteWall(id, authentication.getName());
    }
}
