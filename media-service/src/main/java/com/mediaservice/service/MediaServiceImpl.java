package com.mediaservice.service;

import com.mediaservice.entity.Media;
import com.mediaservice.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

	private final MediaRepository repository;
	private final String UPLOAD_DIR = "uploads/media/";

	@org.springframework.beans.factory.annotation.Value("${gateway.url:https://3.108.190.193.nip.io}")
	private String gatewayUrl;

	@Override
	public Media uploadMedia(MultipartFile file, Integer uploaderId, String altText) throws IOException {
		// 1. Create directory if not exists
		Path uploadPath = Paths.get(UPLOAD_DIR);
		if (!Files.exists(uploadPath))
			Files.createDirectories(uploadPath);

		// 2. Generate unique filename
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		Path filePath = uploadPath.resolve(fileName);

		// 3. Save physical file
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		// 4. Save metadata
		Media media = new Media();
		media.setUploaderId(uploaderId);
		media.setFilename(fileName);
		media.setOriginalName(file.getOriginalFilename());
		media.setMimeType(file.getContentType());
		media.setSizeKb(file.getSize() / 1024);
		media.setAltText(altText);
		
		// Fix: Use Gateway URL for production compatibility
		media.setUrl(gatewayUrl + "/media/display/" + fileName); 

		return repository.save(media);
	}

	@Override
	public void linkToPost(Integer mediaId, Integer postId) {
		Media media = getMediaById(mediaId);
		media.setLinkedPostId(postId);
		repository.save(media);
	}

	@Override
	public void deleteMedia(Integer mediaId) {
		Media media = getMediaById(mediaId);
		media.setDeleted(true); // Soft-delete
		repository.save(media);
	}

	@Override
	public Media getMediaById(Integer mediaId) {
		return repository.findByMediaIdAndIsDeletedFalse(mediaId)
				.orElseThrow(() -> new RuntimeException("Media record not found"));
	}

	// Implement other methods as simple repository calls...
	@Override
	public List<Media> getMediaByUploader(Integer uploaderId) {
		return repository.findByUploaderIdAndIsDeletedFalse(uploaderId);
	}

	@Override
	public List<Media> getMediaByPost(Integer postId) {
		return repository.findByLinkedPostIdAndIsDeletedFalse(postId);
	}

	@Override
	public void unlinkFromPost(Integer mediaId) {
		Media m = getMediaById(mediaId);
		m.setLinkedPostId(null);
		repository.save(m);
	}

	@Override
	public Media updateAltText(Integer mediaId, String altText) {
		Media m = getMediaById(mediaId);
		m.setAltText(altText);
		return repository.save(m);
	}

	@Override
	public void cleanupDeleted() {
		}

	@Override
	public List<Media> getAllMedia() {
		// Fetches everything that isn't soft-deleted
		return repository.findAll().stream().filter(media -> !media.isDeleted()).collect(Collectors.toList());
	}
}