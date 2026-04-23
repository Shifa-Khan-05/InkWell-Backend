package com.taxonomyservice.controller;

import com.taxonomyservice.entity.*;
import com.taxonomyservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/taxonomy")
@RequiredArgsConstructor
public class CategoryResource {
    private final CategoryService service;

    @PostMapping("/categories")
    public ResponseEntity<Category> createCat(@RequestBody Category c) {
        return ResponseEntity.ok(service.createCategory(c));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCats() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<Category> getCat(@PathVariable String slug) {
        return ResponseEntity.ok(service.getCategoryBySlug(slug));
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody Tag t) {
        return ResponseEntity.ok(service.createTag(t));
    }

    @GetMapping("/tags/trending")
    public ResponseEntity<List<Tag>> getTrending() {
        return ResponseEntity.ok(service.getTrendingTags());
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        service.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Requirement: Update post count when a new post is published.
     * Called by Post-Service via Feign Client.
     */
    @PutMapping("/categories/{id}/increment")
    public ResponseEntity<Void> incrementPostCount(@PathVariable Integer id) {
        service.incrementCategoryPostCount(id);
        return ResponseEntity.ok().build();
    }
}