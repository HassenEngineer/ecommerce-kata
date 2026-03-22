package com.example.backend.service;

import com.example.backend.dto.CategoryDto;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.dto.ProductDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Category;
import com.example.backend.model.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryDto create(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (request.getParentCategoryId() != null) {
            Category parent = findCategoryOrThrow(request.getParentCategoryId());
            category.setParentCategory(parent);
        }

        return toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(this::toTreeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        return toTreeDto(findCategoryOrThrow(id));
    }

    public CategoryDto update(Long id, CreateCategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }
            Category parent = findCategoryOrThrow(request.getParentCategoryId());
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        return toDto(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    public void addProduct(Long categoryId, Long productId) {
        Category category = findCategoryOrThrow(categoryId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
        category.getProducts().add(product);
        categoryRepository.save(category);
    }

    public void removeProduct(Long categoryId, Long productId) {
        Category category = findCategoryOrThrow(categoryId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
        category.getProducts().remove(product);
        categoryRepository.save(category);
    }

    public void addSubCategory(Long parentId, Long subId) {
        Category parent = findCategoryOrThrow(parentId);
        Category sub = findCategoryOrThrow(subId);
        if (parentId.equals(subId)) {
            throw new IllegalArgumentException("A category cannot be its own subcategory");
        }
        sub.setParentCategory(parent);
        categoryRepository.save(sub);
    }

    public void removeSubCategory(Long parentId, Long subId) {
        Category sub = findCategoryOrThrow(subId);
        if (sub.getParentCategory() == null || !sub.getParentCategory().getId().equals(parentId)) {
            throw new IllegalArgumentException("Category " + subId + " is not a subcategory of " + parentId);
        }
        sub.setParentCategory(null);
        categoryRepository.save(sub);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subCategories(List.of())
                .products(category.getProducts().stream().map(this::toProductDto).toList())
                .build();
    }

    private CategoryDto toTreeDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subCategories(category.getSubCategories().stream().map(this::toTreeDto).toList())
                .products(category.getProducts().stream().map(this::toProductDto).toList())
                .build();
    }

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
