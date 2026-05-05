package com.taxonomyservice.controller;

import com.taxonomyservice.entity.Category;
import com.taxonomyservice.entity.Tag;
import com.taxonomyservice.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryResourceTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryResource categoryResource;

    private Category category;
    private Tag tag;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1);
        category.setName("Technology");
        category.setSlug("technology");

        tag = new Tag();
        tag.setTagId(1);
        tag.setName("Java");
        tag.setSlug("java");
    }

    @Test
    void createCat_Success() {
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        ResponseEntity<Category> response = categoryResource.createCat(category);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(category);
    }

    @Test
    void getAllCats_Success() {
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        ResponseEntity<List<Category>> response = categoryResource.getAllCats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getCat_Success() {
        when(categoryService.getCategoryBySlug("technology")).thenReturn(category);

        ResponseEntity<Category> response = categoryResource.getCat("technology");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(category);
    }

    @Test
    void createTag_Success() {
        when(categoryService.createTag(any(Tag.class))).thenReturn(tag);

        ResponseEntity<Tag> response = categoryResource.createTag(tag);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tag);
    }

    @Test
    void getTrending_Success() {
        when(categoryService.getTrendingTags()).thenReturn(List.of(tag));

        ResponseEntity<List<Tag>> response = categoryResource.getTrending();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void deleteTag_Success() {
        doNothing().when(categoryService).deleteTag(1);

        ResponseEntity<Void> response = categoryResource.deleteTag(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryService).deleteTag(1);
    }

    @Test
    void incrementPostCount_Success() {
        doNothing().when(categoryService).incrementCategoryPostCount(1);

        ResponseEntity<Void> response = categoryResource.incrementPostCount(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(categoryService).incrementCategoryPostCount(1);
    }
}
