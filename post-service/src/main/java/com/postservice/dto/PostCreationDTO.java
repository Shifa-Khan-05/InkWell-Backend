package com.postservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostCreationDTO {
	private String title;
	private String content;
	private String excerpt;
	private int authorId;
	private String status;
	private Integer categoryId;
	private List<Integer> tagIds;
	private String featuredImageUrl; 
}