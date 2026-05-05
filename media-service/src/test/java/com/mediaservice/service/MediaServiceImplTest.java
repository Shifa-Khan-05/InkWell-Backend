package com.mediaservice.service;

import com.mediaservice.entity.Media;
import com.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository repository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media media;

    @BeforeEach
    void setUp() {
        media = new Media();
        media.setMediaId(1);
        media.setUploaderId(1);
        media.setFilename("test.jpg");
        media.setDeleted(false);
    }

    @Test
    void uploadMedia_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(repository.save(any(Media.class))).thenReturn(media);

        Media result = mediaService.uploadMedia(file, 1, "alt text");
        assertThat(result).isNotNull();
    }

    @Test
    void linkToPost_Success() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(media));
        mediaService.linkToPost(1, 100);
        assertThat(media.getLinkedPostId()).isEqualTo(100);
        verify(repository).save(media);
    }

    @Test
    void deleteMedia_Success() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(media));
        mediaService.deleteMedia(1);
        assertThat(media.isDeleted()).isTrue();
        verify(repository).save(media);
    }

    @Test
    void getMediaById_Success() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(media));
        assertThat(mediaService.getMediaById(1)).isNotNull();
    }

    @Test
    void getMediaByUploader_Success() {
        when(repository.findByUploaderIdAndIsDeletedFalse(1)).thenReturn(List.of(media));
        assertThat(mediaService.getMediaByUploader(1)).hasSize(1);
    }

    @Test
    void getMediaByPost_Success() {
        when(repository.findByLinkedPostIdAndIsDeletedFalse(1)).thenReturn(List.of(media));
        assertThat(mediaService.getMediaByPost(1)).hasSize(1);
    }

    @Test
    void unlinkFromPost_Success() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(media));
        mediaService.unlinkFromPost(1);
        assertThat(media.getLinkedPostId()).isNull();
        verify(repository).save(media);
    }

    @Test
    void updateAltText_Success() {
        when(repository.findByMediaIdAndIsDeletedFalse(1)).thenReturn(Optional.of(media));
        when(repository.save(media)).thenReturn(media);
        Media result = mediaService.updateAltText(1, "new alt");
        assertThat(result.getAltText()).isEqualTo("new alt");
    }

    @Test
    void getAllMedia_Success() {
        when(repository.findAll()).thenReturn(List.of(media));
        assertThat(mediaService.getAllMedia()).hasSize(1);
    }
}
