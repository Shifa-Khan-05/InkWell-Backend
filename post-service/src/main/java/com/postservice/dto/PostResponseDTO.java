package com.postservice.dto;

import lombok.Data;
import lombok.Setter;
import java.io.Serializable; // ✅ Import this

@Data
@Setter

public class PostResponseDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private int postId;
	private String title;
	private String content;
	private String slug;
	private String status;
	private int authorId;
	private String fullName; // Required for the Feign Client result
	private int readTimeMin;
	private int viewCount;
	private int likesCount;
	private String featuredImageUrl; // ✅ Matches frontend postPayload
	private String authorImageUrl; // ✅ ADDED
	private boolean isLikedByCurrentUser; // ✅ ADD THIS
}