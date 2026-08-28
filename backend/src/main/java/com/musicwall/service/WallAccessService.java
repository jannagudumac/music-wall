package com.musicwall.service;

import com.musicwall.entity.MusicWallEntity;
import com.musicwall.exception.ForbiddenException;
import com.musicwall.exception.ResourceNotFoundException;
import com.musicwall.repository.MusicWallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WallAccessService {

    private final MusicWallRepository wallRepository;

    @Transactional(readOnly = true)
    public MusicWallEntity findAccessibleWall(String username, Long wallId) {
        MusicWallEntity wall = findWall(wallId);
        if (isOwner(wall, username) || wall.getMembers().stream()
                .anyMatch(member -> member.getUsername().equals(username))) {
            return wall;
        }
        throw new ForbiddenException("You do not have access to this wall");
    }

    @Transactional(readOnly = true)
    public MusicWallEntity findOwnedWall(String username, Long wallId) {
        MusicWallEntity wall = findWall(wallId);
        if (!isOwner(wall, username)) {
            throw new ForbiddenException("Only the wall owner can perform this action");
        }
        return wall;
    }

    public boolean isOwner(MusicWallEntity wall, String username) {
        return wall.getOwner().getUsername().equals(username);
    }

    private MusicWallEntity findWall(Long wallId) {
        return wallRepository.findById(wallId)
                .orElseThrow(() -> new ResourceNotFoundException("Wall not found"));
    }
}
