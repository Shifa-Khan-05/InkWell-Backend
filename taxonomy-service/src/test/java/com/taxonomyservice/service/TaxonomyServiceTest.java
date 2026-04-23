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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaxonomyServiceTest {

    @Mock
    private CategoryRepository categoryRepo;

    @Mock
    private TagRepository tagRepo;

    @InjectMocks
    private CategoryServiceImpl taxonomyService;

    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setCategoryId(1);
        testCategory.setName("Technology");
        testCategory.setSlug("technology");
        testCategory.setPostCount(0);

        testTag = new Tag();
        testTag.setTagId(1);
        testTag.setName("Java");
        testTag.setUsageCount(10);
    }

    @Test
    void testCreateCategory_Success() {
        when(categoryRepo.save(any(Category.class))).thenReturn(testCategory);

        Category created = taxonomyService.createCategory(testCategory);

        assertNotNull(created);
        assertEquals("Technology", created.getName());
        verify(categoryRepo, times(1)).save(testCategory);
    }

    @Test
    void testGetCategoryBySlug_Success() {
        when(categoryRepo.findBySlug("technology")).thenReturn(Optional.of(testCategory));

        Category found = taxonomyService.getCategoryBySlug("technology");

        assertNotNull(found);
        assertEquals(1, found.getCategoryId());
    }

    @Test
    void testIncrementCategoryPostCount_Success() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(testCategory));
        
        taxonomyService.incrementCategoryPostCount(1);

        // postCount should go from 0 to 1
        assertEquals(1, testCategory.getPostCount());
        verify(categoryRepo, times(1)).save(testCategory);
    }

    @Test
    void testGetTrendingTags_Success() {
        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.setUsageCount(20);

        when(tagRepo.findTopTrendingTags()).thenReturn(Arrays.asList(tag2, testTag));

        List<Tag> trending = taxonomyService.getTrendingTags();

        assertEquals(2, trending.size());
        assertEquals("Spring", trending.get(0).getName()); // Spring has higher usage count
    }

    @Test
    void testIncrementPostCount_ThrowsExceptionWhenNotFound() {
        when(categoryRepo.findById(999)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            taxonomyService.incrementCategoryPostCount(999);
        });

        assertTrue(exception.getMessage().contains("Category not found"));
    }
}