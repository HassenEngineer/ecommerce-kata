package com.example.backend.service;

import com.example.backend.dto.CategoryDto;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Category;
import com.example.backend.model.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    // --- create ---

    @Test
    void create_rootCategory_returnsDto() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Devices", null);

        Category saved = Category.builder()
                .id(1L).name("Electronics").description("Devices")
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Devices");
        assertThat(result.getParentCategoryId()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_withExistingParent_setsParent() {
        Category parent = Category.builder().id(10L).name("Parent").build();
        CreateCategoryRequest request = new CreateCategoryRequest("Child", "Sub", 10L);

        Category saved = Category.builder()
                .id(2L).name("Child").description("Sub").parentCategory(parent)
                .build();

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.create(request);

        assertThat(result.getParentCategoryId()).isEqualTo(10L);
    }

    @Test
    void create_parentNotFound_throwsResourceNotFoundException() {
        CreateCategoryRequest request = new CreateCategoryRequest("Child", "Sub", 99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- getRootCategories ---

    @Test
    void getRootCategories_returnsCategoryTree() {
        Category root = Category.builder()
                .id(1L).name("Root").subCategories(new ArrayList<>()).products(new HashSet<>())
                .build();

        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(List.of(root));

        List<CategoryDto> result = categoryService.getRootCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Root");
    }

    // --- getById ---

    @Test
    void getById_found_returnsCategoryTree() {
        Category category = Category.builder()
                .id(1L).name("Cat").subCategories(new ArrayList<>()).products(new HashSet<>())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cat");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- update ---

    @Test
    void update_nameAndDescription_returnsUpdated() {
        Category existing = Category.builder()
                .id(1L).name("Old").description("OldDesc").products(new HashSet<>())
                .build();
        CreateCategoryRequest request = new CreateCategoryRequest("New", "NewDesc", null);

        Category saved = Category.builder()
                .id(1L).name("New").description("NewDesc").products(new HashSet<>())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.update(1L, request);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("NewDesc");
        assertThat(result.getParentCategoryId()).isNull();
    }

    @Test
    void update_changeParent_setsNewParent() {
        Category existing = Category.builder()
                .id(1L).name("Cat").products(new HashSet<>())
                .build();
        Category newParent = Category.builder().id(5L).name("NewParent").build();
        CreateCategoryRequest request = new CreateCategoryRequest("Cat", null, 5L);

        Category saved = Category.builder()
                .id(1L).name("Cat").parentCategory(newParent).products(new HashSet<>())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.update(1L, request);

        assertThat(result.getParentCategoryId()).isEqualTo(5L);
    }

    @Test
    void update_selfParent_throwsIllegalArgumentException() {
        Category existing = Category.builder()
                .id(1L).name("Cat").products(new HashSet<>())
                .build();
        CreateCategoryRequest request = new CreateCategoryRequest("Cat", null, 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own parent");
    }

    @Test
    void update_categoryNotFound_throwsResourceNotFoundException() {
        CreateCategoryRequest request = new CreateCategoryRequest("Cat", null, null);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void delete_existing_deletesCategory() {
        Category category = Category.builder().id(1L).name("Cat").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- addProduct ---

    @Test
    void addProduct_ok_addsProductToCategory() {
        Category category = Category.builder().id(1L).name("Cat").products(new HashSet<>()).build();
        Product product = Product.builder().id(2L).name("Prod").price(BigDecimal.TEN).stockQuantity(5).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.addProduct(1L, 2L);

        assertThat(category.getProducts()).contains(product);
        verify(categoryRepository).save(category);
    }

    @Test
    void addProduct_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.addProduct(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addProduct_productNotFound_throwsResourceNotFoundException() {
        Category category = Category.builder().id(1L).name("Cat").products(new HashSet<>()).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.addProduct(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- removeProduct ---

    @Test
    void removeProduct_ok_removesProductFromCategory() {
        Product product = Product.builder().id(2L).name("Prod").price(BigDecimal.TEN).stockQuantity(5).build();
        HashSet<Product> products = new HashSet<>();
        products.add(product);
        Category category = Category.builder().id(1L).name("Cat").products(products).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.removeProduct(1L, 2L);

        assertThat(category.getProducts()).doesNotContain(product);
        verify(categoryRepository).save(category);
    }

    // --- addSubCategory ---

    @Test
    void addSubCategory_ok_setsParent() {
        Category parent = Category.builder().id(1L).name("Parent").build();
        Category sub = Category.builder().id(2L).name("Sub").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sub));
        when(categoryRepository.save(any(Category.class))).thenReturn(sub);

        categoryService.addSubCategory(1L, 2L);

        assertThat(sub.getParentCategory()).isEqualTo(parent);
        verify(categoryRepository).save(sub);
    }

    @Test
    void addSubCategory_selfSubcategory_throwsIllegalArgumentException() {
        Category category = Category.builder().id(1L).name("Cat").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.addSubCategory(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own subcategory");
    }

    // --- removeSubCategory ---

    @Test
    void removeSubCategory_ok_removesParent() {
        Category parent = Category.builder().id(1L).name("Parent").build();
        Category sub = Category.builder().id(2L).name("Sub").parentCategory(parent).build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sub));
        when(categoryRepository.save(any(Category.class))).thenReturn(sub);

        categoryService.removeSubCategory(1L, 2L);

        assertThat(sub.getParentCategory()).isNull();
        verify(categoryRepository).save(sub);
    }

    @Test
    void removeSubCategory_notChild_throwsIllegalArgumentException() {
        Category other = Category.builder().id(3L).name("Other").build();
        Category sub = Category.builder().id(2L).name("Sub").parentCategory(other).build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> categoryService.removeSubCategory(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a subcategory");
    }
}
