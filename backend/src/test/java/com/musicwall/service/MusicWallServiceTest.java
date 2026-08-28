package com.musicwall.service;

import com.musicwall.dto.AddWallMemberRequest;
import com.musicwall.dto.CreateMusicWallRequest;
import com.musicwall.entity.MusicWallEntity;
import com.musicwall.entity.UserEntity;
import com.musicwall.repository.MusicItemRepository;
import com.musicwall.repository.MusicSectionRepository;
import com.musicwall.repository.MusicWallRepository;
import com.musicwall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicWallServiceTest {

    @Mock MusicWallRepository wallRepository;
    @Mock UserRepository userRepository;
    @Mock MusicSectionRepository sectionRepository;
    @Mock MusicItemRepository itemRepository;
    @Mock MusicSectionService sectionService;
    @Mock WallAccessService accessService;

    private MusicWallService service;

    @BeforeEach
    void setUp() {
        service = new MusicWallService(
                wallRepository,
                userRepository,
                sectionRepository,
                itemRepository,
                sectionService,
                accessService
        );
    }

    @Test
    void creatorBecomesOwnerWithoutBeingDuplicatedAsMember() {
        UserEntity owner = user(1L, "janna");
        CreateMusicWallRequest request = new CreateMusicWallRequest();
        request.setName("Shared discoveries");
        when(userRepository.findByUsername("janna")).thenReturn(Optional.of(owner));
        when(wallRepository.save(any(MusicWallEntity.class))).thenAnswer(invocation -> {
            MusicWallEntity wall = invocation.getArgument(0);
            wall.setId(10L);
            return wall;
        });

        service.createWall("janna", request);

        ArgumentCaptor<MusicWallEntity> saved = ArgumentCaptor.forClass(MusicWallEntity.class);
        verify(wallRepository).save(saved.capture());
        assertEquals(owner, saved.getValue().getOwner());
        assertTrue(saved.getValue().getMembers().isEmpty());
    }

    @Test
    void ownerCanDirectlyAddARegisteredMember() {
        UserEntity owner = user(1L, "janna");
        UserEntity member = user(2L, "alice");
        MusicWallEntity wall = new MusicWallEntity();
        wall.setId(10L);
        wall.setOwner(owner);
        AddWallMemberRequest request = new AddWallMemberRequest();
        request.setUsername("alice");
        when(accessService.findOwnedWall("janna", 10L)).thenReturn(wall);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(member));

        service.addMember(10L, "janna", request);

        assertTrue(wall.getMembers().contains(member));
        verify(wallRepository).save(wall);
    }

    private UserEntity user(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
