package com.mediaservice.controller;

import com.mediaservice.entity.Media;
import com.mediaservice.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaResourceTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaResource mediaResource;

    private Media media;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        media = new Media();
        media.setMediaId(1);
        media.setFilename("test.jpg");
        media.setUploaderId(1);

        mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());
    }

    @Test
    void uploadFile_Success() throws Exception {
        when(mediaService.uploadMedia(mockFile, 1, "Alt text")).thenReturn(media);

        ResponseEntity<Media> response = mediaResource.uploadFile(mockFile, 1, "Alt text");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(media);
    }

    @Test
    void getByUser_Success() {
        when(mediaService.getMediaByUploader(1)).thenReturn(List.of(media));

        ResponseEntity<List<Media>> response = mediaResource.getByUser(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void updateAlt_Success() {
        when(mediaService.updateAltText(1, "New Alt")).thenReturn(media);

        ResponseEntity<Media> response = mediaResource.updateAlt(1, "New Alt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(media);
    }

    @Test
    void delete_Success() {
        doNothing().when(mediaService).deleteMedia(1);

        ResponseEntity<Void> response = mediaResource.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mediaService).deleteMedia(1);
    }

    @Test
    void getDetails_Success() {
        when(mediaService.getMediaById(1)).thenReturn(media);

        ResponseEntity<Media> response = mediaResource.getDetails(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(media);
    }

    @Test
    void getAllMedia_Success() {
        when(mediaService.getAllMedia()).thenReturn(List.of(media));

        ResponseEntity<List<Media>> response = mediaResource.getAllMedia();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}
