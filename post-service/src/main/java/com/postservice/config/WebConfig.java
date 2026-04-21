package com.postservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This targets the physical folder shown in your screenshot
        String uploadPath = Paths.get("post_uploads").toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/post_uploads/**")
                .addResourceLocations(uploadPath) // Example: file:/E:/InkWell-project/.../post_uploads/
                .setCachePeriod(0);
    }
}