package com.mediaservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mediaservice.entity.Media;
import com.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

    @Mock
    private MediaRepository repository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media mockMedia;

    @BeforeEach
    void setUp() {
        mockMedia = new Media();
        mockMedia.setMediaId(1);
        mockMedia.setUploaderId(101);
        mockMedia.setFilename("12345_test.jpg");
        mockMedia.setUrl("http://localhost:8087/media/display/12345_test.jpg");
        mockMedia.setDeleted(false);
    }

    @Test
    void testUploadMedia_Success() throws IOException {
        // Prepare a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        // Define repository behavior
        when(repository.save(any(Media.class))).thenReturn(mockMedia);

        // Execute
        Media result = mediaService.uploadMedia(file, 101, "Test Image");

        // Verify
        assertNotNull(result);
        assertEquals("12345_test.jpg", result.getFilename());
        verify(repository, times(1)).save(any(Media.class));
    }

    @Test
    void testGetMediaById_Found() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(mockMedia));

        Media result = mediaService.getMediaById(1);

        assertNotNull(result);
        assertEquals(1, result.getMediaId());
        assertFalse(result.isDeleted());
    }

    @Test
    void testGetMediaById_NotFound() {
        when(repository.findByMediaIdAndIsDeletedFalse(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            mediaService.getMediaById(99);
        });
    }

    @Test
    void testGetAllMedia_FiltersDeleted() {
        Media deletedMedia = new Media();
        deletedMedia.setDeleted(true);

        when(repository.findAll()).thenReturn(Arrays.asList(mockMedia, deletedMedia));

        List<Media> result = mediaService.getAllMedia();

        // Should only return 1 item because the other is marked deleted
        assertEquals(1, result.size());
        assertFalse(result.get(0).isDeleted());
    }

    @Test
    void testDeleteMedia_SoftDelete() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(mockMedia));
        
        mediaService.deleteMedia(1);

        // Verify that the status was changed and saved
        assertTrue(mockMedia.isDeleted());
        verify(repository, times(1)).save(mockMedia);
    }
}