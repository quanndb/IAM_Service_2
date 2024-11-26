package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    @PreAuthorize("hasPermission('products', 'READ')")
    public ApiResponse<String> getProducts(){
        return ApiResponse.<String>builder()
                .code(200)
                .message("ok")
                .build();
    }
}
