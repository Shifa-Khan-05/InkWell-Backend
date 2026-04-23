package com.mediaservice.repository;

import com.mediaservice.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Integer> {
    
    List<Media> findByUploaderIdAndIsDeletedFalse(Integer uploaderId);
    
    List<Media> findByLinkedPostIdAndIsDeletedFalse(Integer linkedPostId);
    
    Optional<Media> findByMediaIdAndIsDeletedFalse(Integer mediaId);
    
    List<Media> findByMimeType(String mimeType);
    
    long countByUploaderId(Integer uploaderId);
    
    // For cleanup operations
    List<Media> findByIsDeletedTrue();
}