package com.taxonomyservice.repository;

import com.taxonomyservice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentCategoryId(Integer parentCategoryId);
}