package com.musicwall.service;

import com.musicwall.dto.CreateMusicItemRequest;
import com.musicwall.entity.MusicSectionEntity;
import com.musicwall.entity.MusicWallEntity;
import com.musicwall.exception.BusinessException;
import com.musicwall.repository.AlbumRepository;
import com.musicwall.repository.MusicItemRepository;
import com.musicwall.repository.MusicSectionRepository;
import com.musicwall.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicItemServiceTest {

    @Mock MusicItemRepository itemRepository;
    @Mock MusicSectionRepository sectionRepository;
    @Mock WallAccessService accessService;
    @Mock TrackRepository trackRepository;
    @Mock AlbumRepository albumRepository;

    @Test
    void catalogueItemRequiresExactlyOneTrackOrAlbumReference() {
        MusicWallEntity wall = new MusicWallEntity();
        wall.setId(4L);
        MusicSectionEntity section = new MusicSectionEntity();
        section.setId(8L);
        section.setWall(wall);
        when(accessService.findAccessibleWall("janna", 4L)).thenReturn(wall);
        when(sectionRepository.findById(8L)).thenReturn(Optional.of(section));
        MusicItemService service = new MusicItemService(
                itemRepository, sectionRepository, accessService, trackRepository, albumRepository
        );
        CreateMusicItemRequest request = new CreateMusicItemRequest();
        request.setStatus("TO_LISTEN");
        request.setCatalogTrackId(2L);
        request.setCatalogAlbumId(3L);

        assertThrows(
                BusinessException.class,
                () -> service.createItem("janna", 4L, 8L, request)
        );
    }
}
