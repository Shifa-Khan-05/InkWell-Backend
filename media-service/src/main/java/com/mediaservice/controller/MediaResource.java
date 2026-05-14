package com.mediaservice.controller;

import com.mediaservice.entity.Media;
import com.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaResource {

	private final MediaService service;

	@PostMapping("/upload")
	public ResponseEntity<Media> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("uploaderId") Integer uploaderId, @RequestParam(required = false) String altText)
			throws Exception {
		return ResponseEntity.ok(service.uploadMedia(file, uploaderId, altText));
	}

	@GetMapping("/uploader/{uploaderId}")
	public ResponseEntity<List<Media>> getByUser(@PathVariable Integer uploaderId) {
		return ResponseEntity.ok(service.getMediaByUploader(uploaderId));
	}

	@PutMapping("/{id}/alt")
	public ResponseEntity<Media> updateAlt(@PathVariable Integer id, @RequestBody String altText) {
		return ResponseEntity.ok(service.updateAltText(id, altText));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.deleteMedia(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/display/{filename}")
	public ResponseEntity<Resource> displayImage(@PathVariable String filename) {
		try {
			Path path = Paths.get("uploads/media/").resolve(filename);
			Resource resource = new UrlResource(path.toUri());
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG) // You can make this dynamic
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/detail/{id}")
	public ResponseEntity<Media> getDetails(@PathVariable Integer id) {
		return ResponseEntity.ok(service.getMediaById(id));
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Media>> getAllMedia() {
		return ResponseEntity.ok(service.getAllMedia());
	}
}