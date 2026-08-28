package com.musicwall.service;

import com.musicwall.entity.MusicWallEntity;
import com.musicwall.entity.UserEntity;
import com.musicwall.exception.ForbiddenException;
import com.musicwall.repository.MusicWallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WallAccessServiceTest {

    @Mock MusicWallRepository wallRepository;

    @Test
    void directMemberCanAccessWallButCannotUseOwnerOperation() {
        UserEntity owner = user("janna");
        UserEntity member = user("alice");
        MusicWallEntity wall = new MusicWallEntity();
        wall.setId(10L);
        wall.setOwner(owner);
        wall.getMembers().add(member);
        when(wallRepository.findById(10L)).thenReturn(Optional.of(wall));
        WallAccessService service = new WallAccessService(wallRepository);

        assertEquals(wall, service.findAccessibleWall("alice", 10L));
        assertThrows(ForbiddenException.class, () -> service.findOwnedWall("alice", 10L));
    }

    private UserEntity user(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        return user;
    }
}
