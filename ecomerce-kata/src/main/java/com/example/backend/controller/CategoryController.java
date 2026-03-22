package com.example.backend.controller;


import com.example.backend.dto.CategoryDto;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryDto> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @PutMapping("/{catId}/products/{prodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addProduct(@PathVariable Long catId, @PathVariable Long prodId) {
        categoryService.addProduct(catId, prodId);
    }

    @DeleteMapping("/{catId}/products/{prodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProduct(@PathVariable Long catId, @PathVariable Long prodId) {
        categoryService.removeProduct(catId, prodId);
    }

    @PutMapping("/{catId}/subcategories/{subId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSubCategory(@PathVariable Long catId, @PathVariable Long subId) {
        categoryService.addSubCategory(catId, subId);
    }

    @DeleteMapping("/{catId}/subcategories/{subId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSubCategory(@PathVariable Long catId, @PathVariable Long subId) {
        categoryService.removeSubCategory(catId, subId);
    }
}
