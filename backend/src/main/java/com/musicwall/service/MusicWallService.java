package com.musicwall.service;

import com.musicwall.dto.AddWallMemberRequest;
import com.musicwall.dto.CreateMusicWallRequest;
import com.musicwall.dto.MusicWallDTO;
import com.musicwall.dto.MusicWallDetailDTO;
import com.musicwall.dto.UpdateWallAppearanceRequest;
import com.musicwall.dto.UserSearchDTO;
import com.musicwall.dto.WallMemberDTO;
import com.musicwall.entity.MusicWallEntity;
import com.musicwall.entity.UserEntity;
import com.musicwall.exception.BusinessException;
import com.musicwall.exception.ResourceNotFoundException;
import com.musicwall.repository.MusicItemRepository;
import com.musicwall.repository.MusicSectionRepository;
import com.musicwall.repository.MusicWallRepository;
import com.musicwall.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MusicWallService {

    private final MusicWallRepository musicWallRepository;
    private final UserRepository userRepository;
    private final MusicSectionRepository musicSectionRepository;
    private final MusicItemRepository musicItemRepository;
    private final MusicSectionService musicSectionService;
    private final WallAccessService wallAccessService;

    @Transactional
    public MusicWallDTO createWall(String username, CreateMusicWallRequest request) {
        UserEntity owner = findUser(username);
        MusicWallEntity wall = new MusicWallEntity();
        wall.setName(request.getName());
        wall.setDescription(request.getDescription());
        wall.setWallpaper(normalizeWallpaper(request.getWallpaper()));
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        wall.setOwner(owner);
        return convertToDTO(musicWallRepository.save(wall));
    }

    @Transactional(readOnly = true)
    public List<MusicWallDTO> getWallsForUser(String username) {
        return musicWallRepository
                .findDistinctByOwnerUsernameOrMembersUsernameOrderByIdDesc(username, username)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicWallDetailDTO getWall(Long id, String username) {
        MusicWallEntity wall = wallAccessService.findAccessibleWall(username, id);
        MusicWallDetailDTO dto = new MusicWallDetailDTO();
        dto.setId(wall.getId());
        dto.setName(wall.getName());
        dto.setDescription(wall.getDescription());
        dto.setOwnerUsername(wall.getOwner().getUsername());
        dto.setWallpaper(normalizeWallpaper(wall.getWallpaper()));
        dto.setWallColor(normalizeWallColor(wall.getWallColor()));
        dto.setSections(musicSectionService.getSectionsForWall(username, id));
        return dto;
    }

    @Transactional
    public MusicWallDTO updateWall(Long id, String username, CreateMusicWallRequest request) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(username, id);
        wall.setName(request.getName());
        wall.setDescription(request.getDescription());
        wall.setWallpaper(normalizeWallpaper(request.getWallpaper()));
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        return convertToDTO(musicWallRepository.save(wall));
    }

    @Transactional
    public MusicWallDTO updateWallAppearance(
            Long id,
            String username,
            UpdateWallAppearanceRequest request
    ) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(username, id);
        wall.setWallpaper(normalizeWallpaper(request.getWallpaper()));
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        return convertToDTO(musicWallRepository.save(wall));
    }

    @Transactional(readOnly = true)
    public List<WallMemberDTO> getMembers(Long wallId, String username) {
        MusicWallEntity wall = wallAccessService.findAccessibleWall(username, wallId);
        return wall.getMembers().stream()
                .map(member -> new WallMemberDTO(member.getUsername()))
                .sorted((first, second) -> first.getUsername()
                        .compareToIgnoreCase(second.getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSearchDTO> searchMemberCandidates(
            Long wallId,
            String ownerUsername,
            String query
    ) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(ownerUsername, wallId);
        String cleanedQuery = query == null ? "" : query.trim();
        if (cleanedQuery.length() < 2) {
            throw new BusinessException("Enter at least two characters");
        }

        Set<String> memberNames = wall.getMembers().stream()
                .map(UserEntity::getUsername)
                .collect(Collectors.toSet());
        return userRepository.searchByUsername(cleanedQuery, ownerUsername).stream()
                .filter(user -> !memberNames.contains(user.getUsername()))
                .limit(20)
                .map(user -> new UserSearchDTO(user.getUsername()))
                .toList();
    }

    @Transactional
    public WallMemberDTO addMember(
            Long wallId,
            String ownerUsername,
            AddWallMemberRequest request
    ) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(ownerUsername, wallId);
        String memberUsername = request.getUsername().trim();
        if (ownerUsername.equals(memberUsername)) {
            throw new BusinessException("The owner is already part of this wall");
        }

        UserEntity member = findUser(memberUsername);
        boolean alreadyMember = wall.getMembers().stream()
                .anyMatch(existing -> existing.getUsername().equals(memberUsername));
        if (alreadyMember) {
            throw new BusinessException("This user is already a member");
        }

        wall.getMembers().add(member);
        musicWallRepository.save(wall);
        return new WallMemberDTO(member.getUsername());
    }

    @Transactional
    public void removeMember(Long wallId, String ownerUsername, String memberUsername) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(ownerUsername, wallId);
        boolean removed = wall.getMembers().removeIf(
                member -> member.getUsername().equals(memberUsername)
        );
        if (!removed) {
            throw new ResourceNotFoundException("Wall member not found");
        }
        musicWallRepository.save(wall);
    }

    @Transactional
    public void deleteWall(Long id, String username) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(username, id);
        musicSectionRepository.findByWallIdOrderByIdAsc(id).forEach(section ->
                musicItemRepository.deleteBySectionId(section.getId())
        );
        musicSectionRepository.deleteByWallId(id);
        musicWallRepository.delete(wall);
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private MusicWallDTO convertToDTO(MusicWallEntity wall) {
        MusicWallDTO dto = new MusicWallDTO();
        dto.setId(wall.getId());
        dto.setName(wall.getName());
        dto.setDescription(wall.getDescription());
        dto.setOwnerUsername(wall.getOwner().getUsername());
        dto.setWallpaper(normalizeWallpaper(wall.getWallpaper()));
        dto.setWallColor(normalizeWallColor(wall.getWallColor()));
        return dto;
    }

    private String normalizeWallpaper(String wallpaper) {
        if (wallpaper == null || wallpaper.isBlank()) {
            return "NONE";
        }
        return wallpaper.matches("IMAGE_[1-9]") ? wallpaper : "NONE";
    }

    private String normalizeWallColor(String wallColor) {
        return wallColor == null || wallColor.isBlank() ? "#FFFFFF" : wallColor;
    }
}
