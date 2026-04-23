package com.taxonomyservice.service;

import com.taxonomyservice.entity.*;
import com.taxonomyservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
	private final CategoryRepository categoryRepo;
	private final TagRepository tagRepo;
    
	
	@Override
	public Category createCategory(Category c) {
		return categoryRepo.save(c);
	}

	@Override
	public Category getCategoryBySlug(String s) {
		return categoryRepo.findBySlug(s).orElse(null);
	}

	@Override
	public List<Category> getAllCategories() {
		return categoryRepo.findAll();
	}

	@Override
	public void deleteCategory(Integer id) {
		categoryRepo.deleteById(id);
	}

	@Override
	public Tag createTag(Tag t) {
		return tagRepo.save(t);
	}

	@Override
	public List<Tag> getAllTags() {
		return tagRepo.findAll();
	}

	@Override
	public List<Tag> getTrendingTags() {
		return tagRepo.findTopTrendingTags();
	}

	@Override
	public void deleteTag(Integer id) {
		tagRepo.deleteById(id);
	}

	@Override
	public void incrementCategoryPostCount(Integer id) {
		Category cat = categoryRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

		cat.setPostCount(cat.getPostCount() + 1);
		categoryRepo.save(cat);
	}
}