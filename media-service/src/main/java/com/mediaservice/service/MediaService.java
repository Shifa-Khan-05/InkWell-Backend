package com.mediaservice.service;

import com.mediaservice.entity.Media;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface MediaService {
    Media uploadMedia(MultipartFile file, Integer uploaderId, String altText) throws IOException;
    
    Media getMediaById(Integer mediaId);
    
    List<Media> getMediaByUploader(Integer uploaderId);
    
    List<Media> getMediaByPost(Integer postId);
    
    void deleteMedia(Integer mediaId); // Soft-delete
    
    Media updateAltText(Integer mediaId, String altText);
    
    void linkToPost(Integer mediaId, Integer postId);
    
    void unlinkFromPost(Integer mediaId);
    
    List<Media> getAllMedia();
    
    void cleanupDeleted(); // Permanently remove files marked as deleted
}