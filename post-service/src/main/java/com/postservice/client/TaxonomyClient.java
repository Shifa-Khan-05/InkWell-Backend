package com.postservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "taxonomy-service", url = "http://localhost:8080/taxonomy")
public interface TaxonomyClient {

    @PutMapping("/categories/{id}/increment")
    void incrementPostCount(@PathVariable("id") Integer id);
}