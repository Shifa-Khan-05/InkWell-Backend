package com.taxonomyservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    
    @Column(unique = true)
    private String name;
    
    @Column(unique = true)
    private String slug;
    
    private String description;
    private Integer parentCategoryId; // For hierarchical structure
    private Integer postCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}