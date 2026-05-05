package com.taxonomyservice.service;

import com.taxonomyservice.entity.Category;
import com.taxonomyservice.entity.Tag;
import com.taxonomyservice.repository.CategoryRepository;
import com.taxonomyservice.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepo;

    @Mock
    private TagRepository tagRepo;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private Tag tag;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1);
        category.setName("Tech");
        category.setSlug("tech");
        category.setPostCount(0);

        tag = new Tag();
        tag.setTagId(1);
        tag.setName("Java");
    }

    @Test
    void createCategory_Success() {
        when(categoryRepo.save(any(Category.class))).thenReturn(category);
        assertThat(categoryService.createCategory(category)).isNotNull();
    }

    @Test
    void getCategoryBySlug_Success() {
        when(categoryRepo.findBySlug("tech")).thenReturn(Optional.of(category));
        assertThat(categoryService.getCategoryBySlug("tech")).isNotNull();
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepo.findAll()).thenReturn(List.of(category));
        assertThat(categoryService.getAllCategories()).hasSize(1);
    }

    @Test
    void deleteCategory_Success() {
        categoryService.deleteCategory(1);
        verify(categoryRepo).deleteById(1);
    }

    @Test
    void createTag_Success() {
        when(tagRepo.save(any(Tag.class))).thenReturn(tag);
        assertThat(categoryService.createTag(tag)).isNotNull();
    }

    @Test
    void getAllTags_Success() {
        when(tagRepo.findAll()).thenReturn(List.of(tag));
        assertThat(categoryService.getAllTags()).hasSize(1);
    }

    @Test
    void getTrendingTags_Success() {
        when(tagRepo.findTopTrendingTags()).thenReturn(List.of(tag));
        assertThat(categoryService.getTrendingTags()).hasSize(1);
    }

    @Test
    void deleteTag_Success() {
        categoryService.deleteTag(1);
        verify(tagRepo).deleteById(1);
    }

    @Test
    void incrementCategoryPostCount_Success() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        categoryService.incrementCategoryPostCount(1);
        assertThat(category.getPostCount()).isEqualTo(1);
        verify(categoryRepo).save(category);
    }

    @Test
    void incrementCategoryPostCount_NotFound() {
        when(categoryRepo.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.incrementCategoryPostCount(1))
                .isInstanceOf(RuntimeException.class);
    }
}
