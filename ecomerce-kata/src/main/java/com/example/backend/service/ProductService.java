package com.example.backend.service;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.dto.ProductDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
        return toDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAll() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        return toDto(findProductOrThrow(id));
    }

    public ProductDto update(Long id, CreateProductRequest request) {
        Product product = findProductOrThrow(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        return toDto(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        product.getCategories().forEach(cat -> cat.getProducts().remove(product));
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
