package com.example.backend.service;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.dto.ProductDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Category;
import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // --- create ---

    @Test
    void create_validRequest_returnsProductDto() {
        CreateProductRequest request = new CreateProductRequest("Laptop", BigDecimal.valueOf(999.99), 10);

        Product saved = Product.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto result = productService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
        assertThat(result.getStockQuantity()).isEqualTo(10);
    }

    // --- getAll ---

    @Test
    void getAll_returnsList() {
        Product p1 = Product.builder().id(1L).name("A").price(BigDecimal.ONE).stockQuantity(1).build();
        Product p2 = Product.builder().id(2L).name("B").price(BigDecimal.TEN).stockQuantity(2).build();

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductDto> result = productService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A");
        assertThat(result.get(1).getName()).isEqualTo("B");
    }

    // --- getById ---

    @Test
    void getById_found_returnsProductDto() {
        Product product = Product.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto result = productService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- update ---

    @Test
    void update_validRequest_returnsUpdatedDto() {
        Product existing = Product.builder()
                .id(1L).name("Old").price(BigDecimal.ONE).stockQuantity(1)
                .build();
        CreateProductRequest request = new CreateProductRequest("New", BigDecimal.TEN, 20);

        Product saved = Product.builder()
                .id(1L).name("New").price(BigDecimal.TEN).stockQuantity(20)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto result = productService.update(1L, request);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.getStockQuantity()).isEqualTo(20);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        CreateProductRequest request = new CreateProductRequest("X", BigDecimal.ONE, 1);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void delete_existing_cleansUpCategoriesAndDeletes() {
        HashSet<Product> catProducts = new HashSet<>();
        Product product = Product.builder()
                .id(1L).name("Prod").price(BigDecimal.ONE).stockQuantity(1)
                .build();
        catProducts.add(product);

        Category category = Category.builder().id(10L).name("Cat").products(catProducts).build();
        product.setCategories(Set.of(category));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        assertThat(category.getProducts()).doesNotContain(product);
        verify(productRepository).delete(product);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
