package com.postservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostCreationDTO {
	private String title;
	private String content;
	private String excerpt;
	private Integer authorId;
	private String status;
	private Integer categoryId;
	private List<Integer> tagIds;
	private String featuredImageUrl; 
}