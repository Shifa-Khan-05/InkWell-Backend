package com.taxonomyservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;
    
    @Column(unique = true)
    private String name;
    
    @Column(unique = true)
    private String slug;
    
    private Integer usageCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}