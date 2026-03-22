package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    private Long parentCategoryId;
    private List<CategoryDto> subCategories;
    private List<ProductDto> products;
}
