package com.taxonomyservice.service;

import com.taxonomyservice.entity.*;
import java.util.List;

public interface CategoryService {
	Category createCategory(Category category);

	Category getCategoryBySlug(String slug);

	List<Category> getAllCategories();

	void deleteCategory(Integer id);

	Tag createTag(Tag tag);

	List<Tag> getAllTags();

	List<Tag> getTrendingTags();

	void deleteTag(Integer id);
	
	void incrementCategoryPostCount(Integer id);
}