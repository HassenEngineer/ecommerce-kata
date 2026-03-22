package com.example.backend.controller;

import com.example.backend.dto.ProductDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    // --- POST /products ---

    @Test
    void createProduct_valid_returns201() throws Exception {
        ProductDto dto = ProductDto.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .build();

        when(productService.create(any())).thenReturn(dto);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":999.99,\"stockQuantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void createProduct_blankName_returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":10.0,\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativePrice_returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":-1,\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativeStock_returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":10.0,\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /products ---

    @Test
    void getAll_returns200() throws Exception {
        ProductDto dto = ProductDto.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .build();

        when(productService.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    // --- GET /products/{id} ---

    @Test
    void getById_found_returns200() throws Exception {
        ProductDto dto = ProductDto.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .build();

        when(productService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(productService.getById(99L)).thenThrow(new ResourceNotFoundException("Product not found with id 99"));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id 99"));
    }

    // --- PUT /products/{id} ---

    @Test
    void updateProduct_valid_returns200() throws Exception {
        ProductDto dto = ProductDto.builder()
                .id(1L).name("Updated").price(BigDecimal.valueOf(500)).stockQuantity(20)
                .build();

        when(productService.update(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"price\":500,\"stockQuantity\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.price").value(500));
    }

    // --- DELETE /products/{id} ---

    @Test
    void deleteProduct_returns204() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }
}
