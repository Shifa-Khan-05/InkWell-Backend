package com.postservice.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter

public class PostCreationDTO {
	private String title;
	private String content;
	private String excerpt;
	private String featuredImageUrl;
	private int authorId;
	private String status; //
	
}