package com.example.backend.controller;

import com.example.backend.dto.CategoryDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    // --- POST /categories ---

    @Test
    void createCategory_valid_returns201() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L).name("Electronics").description("Devices")
                .subCategories(List.of()).products(List.of())
                .build();

        when(categoryService.create(any())).thenReturn(dto);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Electronics\",\"description\":\"Devices\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /categories ---

    @Test
    void getRootCategories_returns200() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L).name("Root").subCategories(List.of()).products(List.of())
                .build();

        when(categoryService.getRootCategories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Root"));
    }

    // --- GET /categories/{id} ---

    @Test
    void getById_found_returns200() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L).name("Cat").subCategories(List.of()).products(List.of())
                .build();

        when(categoryService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cat"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(categoryService.getById(99L)).thenThrow(new ResourceNotFoundException("Category not found with id 99"));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id 99"));
    }

    // --- PUT /categories/{id} ---

    @Test
    void updateCategory_valid_returns200() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L).name("Updated").description("NewDesc")
                .subCategories(List.of()).products(List.of())
                .build();

        when(categoryService.update(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"description\":\"NewDesc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // --- DELETE /categories/{id} ---

    @Test
    void deleteCategory_returns204() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent());
    }

    // --- PUT /categories/{catId}/products/{prodId} ---

    @Test
    void addProduct_returns204() throws Exception {
        doNothing().when(categoryService).addProduct(1L, 2L);

        mockMvc.perform(put("/categories/1/products/2"))
                .andExpect(status().isNoContent());
    }

    // --- DELETE /categories/{catId}/products/{prodId} ---

    @Test
    void removeProduct_returns204() throws Exception {
        doNothing().when(categoryService).removeProduct(1L, 2L);

        mockMvc.perform(delete("/categories/1/products/2"))
                .andExpect(status().isNoContent());
    }

    // --- PUT /categories/{catId}/subcategories/{subId} ---

    @Test
    void addSubCategory_returns204() throws Exception {
        doNothing().when(categoryService).addSubCategory(1L, 2L);

        mockMvc.perform(put("/categories/1/subcategories/2"))
                .andExpect(status().isNoContent());
    }

    // --- DELETE /categories/{catId}/subcategories/{subId} ---

    @Test
    void removeSubCategory_returns204() throws Exception {
        doNothing().when(categoryService).removeSubCategory(1L, 2L);

        mockMvc.perform(delete("/categories/1/subcategories/2"))
                .andExpect(status().isNoContent());
    }
}
