package com.taxonomyservice.repository;

import com.taxonomyservice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findBySlug(String slug);
    
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopTrendingTags();
}