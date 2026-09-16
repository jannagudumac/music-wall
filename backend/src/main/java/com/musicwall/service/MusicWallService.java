package com.musicwall.service;

import com.musicwall.dto.MusicWallDTO;
import com.musicwall.dto.UpdateWallAppearanceRequest;
import com.musicwall.dto.UserDTO;
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

// The service manages walls and sharing, and checks owner-only actions.
@Service
// Lombok creates the constructor; Spring supplies the required dependencies.
@RequiredArgsConstructor
public class MusicWallService {

    private final MusicWallRepository musicWallRepository;
    private final UserRepository userRepository;
    private final MusicSectionRepository musicSectionRepository;
    private final MusicItemRepository musicItemRepository;
    private final MusicSectionService musicSectionService;
    private final WallAccessService wallAccessService;

    // Create the wall in a transaction: all database changes succeed together or are cancelled.
    @Transactional
    public MusicWallDTO createWall(String username, MusicWallDTO request) {
        // Set the owner from the authenticated user, not from the request.
        UserEntity owner = findUser(username);
        MusicWallEntity wall = new MusicWallEntity();
        wall.setName(request.getName());
        wall.setWallpaper(request.getWallpaper());
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        wall.setOwner(owner);
        return convertToDTO(musicWallRepository.save(wall));
    }

    // Read the user's owned and shared walls without changing the database.
    @Transactional(readOnly = true)
    public List<MusicWallDTO> getWallsForUser(String username) {
        return musicWallRepository
                .findDistinctByOwnerUsernameOrMembersUsernameOrderByIdDesc(username, username)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Check access before loading the wall's details.
    @Transactional(readOnly = true)
    public MusicWallDTO getWall(Long id, String username) {
        MusicWallEntity wall = wallAccessService.findAccessibleWall(username, id);
        MusicWallDTO dto = convertToDTO(wall);
        // Include sections only in the detail response to keep list responses smaller.
        dto.setSections(musicSectionService.getSectionsForWall(username, id));
        return dto;
    }

    // Only the owner can change the wall's settings.
    @Transactional
    public MusicWallDTO updateWall(Long id, String username, MusicWallDTO request) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(username, id);
        // Copy editable fields only; the request cannot replace the id, owner or sections.
        wall.setName(request.getName());
        wall.setWallpaper(request.getWallpaper());
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        return convertToDTO(musicWallRepository.save(wall));
    }

    // Change the background without requiring a new wall name.
    @Transactional
    public MusicWallDTO updateWallAppearance(
            Long id,
            String username,
            UpdateWallAppearanceRequest request
    ) {
        MusicWallEntity wall = wallAccessService.findOwnedWall(username, id);
        wall.setWallpaper(request.getWallpaper());
        wall.setWallColor(normalizeWallColor(request.getWallColor()));
        return convertToDTO(musicWallRepository.save(wall));
    }

    // Return the members' usernames in alphabetical order.
    @Transactional(readOnly = true)
    public List<UserDTO> getMembers(Long wallId, String username) {
        MusicWallEntity wall = wallAccessService.findAccessibleWall(username, wallId);
        return wall.getMembers().stream()
                .map(member -> new UserDTO(member.getUsername()))
                .sorted((first, second) -> first.getUsername()
                        .compareToIgnoreCase(second.getUsername()))
                .toList();
    }

    // Only the owner can search for new members; require at least two characters.
    @Transactional(readOnly = true)
    public List<UserDTO> searchMemberCandidates(
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
        // Exclude the owner and users who are already members.
        return userRepository.searchByUsername(cleanedQuery, ownerUsername).stream()
                .filter(user -> !memberNames.contains(user.getUsername()))
                .limit(20)
                .map(user -> new UserDTO(user.getUsername()))
                .toList();
    }

    // Only the owner can add a registered user; reject duplicate membership.
    @Transactional
    public UserDTO addMember(
            Long wallId,
            String ownerUsername,
            UserDTO request
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

        // Save the wall-member link, not a new user account.
        wall.getMembers().add(member);
        musicWallRepository.save(wall);
        return new UserDTO(member.getUsername());
    }

    // Remove the membership without deleting the user account.
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
        // Delete items, then sections, then the wall in one transaction to respect foreign keys.
        musicSectionRepository.findByWallIdOrderByIdAsc(id).forEach(section ->
                musicItemRepository.deleteBySectionId(section.getId())
        );
        musicSectionRepository.deleteByWallId(id);
        musicWallRepository.delete(wall);
    }

    // Report a missing account with a not-found error.
    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Build a DTO with the owner's username; list responses leave sections out.
    private MusicWallDTO convertToDTO(MusicWallEntity wall) {
        MusicWallDTO dto = new MusicWallDTO();
        dto.setId(wall.getId());
        dto.setName(wall.getName());
        dto.setOwnerUsername(wall.getOwner().getUsername());
        dto.setWallpaper(wall.getWallpaper());
        dto.setWallColor(normalizeWallColor(wall.getWallColor()));
        return dto;
    }

    // Use white when the color is missing or blank.
    private String normalizeWallColor(String wallColor) {
        return wallColor == null || wallColor.isBlank() ? "#FFFFFF" : wallColor;
    }
}
