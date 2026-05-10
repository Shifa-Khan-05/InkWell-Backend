package com.websitecontroller.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MEDIA-SERVICE")
public interface MediaClient {
    @GetMapping("/media/uploader/{uploaderId}")
    List<Map<String, Object>> getMediaByUploader(@PathVariable("uploaderId") Integer uploaderId);

    @GetMapping("/media/all")
    List<Map<String, Object>> getAllMedia();
}
