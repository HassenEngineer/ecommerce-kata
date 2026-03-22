package com.example.backend.controller;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    // --- POST /carts ---

    @Test
    void createCart_returns201() throws Exception {
        CartDto dto = CartDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .total(BigDecimal.ZERO)
                .build();

        when(cartService.createCart()).thenReturn(dto);

        mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(0));
    }

    // --- GET /carts/{id} ---

    @Test
    void getCart_found_returns200() throws Exception {
        CartItemDto itemDto = CartItemDto.builder()
                .id(100L).productId(10L).productName("Laptop")
                .productPrice(BigDecimal.valueOf(500)).quantity(2)
                .subtotal(BigDecimal.valueOf(1000))
                .build();

        CartDto dto = CartDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of(itemDto))
                .total(BigDecimal.valueOf(1000))
                .build();

        when(cartService.getCart(1L)).thenReturn(dto);

        mockMvc.perform(get("/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.total").value(1000));
    }

    @Test
    void getCart_notFound_returns404() throws Exception {
        when(cartService.getCart(99L))
                .thenThrow(new ResourceNotFoundException("Cart not found with id 99"));

        mockMvc.perform(get("/carts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart not found with id 99"));
    }

    // --- POST /carts/{id}/items ---

    @Test
    void addItem_valid_returns201() throws Exception {
        CartItemDto dto = CartItemDto.builder()
                .id(100L).productId(10L).productName("Laptop")
                .productPrice(BigDecimal.valueOf(500)).quantity(2)
                .subtotal(BigDecimal.valueOf(1000))
                .build();

        when(cartService.addItemToCart(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":10,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void addItem_missingProductId_returns400() throws Exception {
        mockMvc.perform(post("/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_zeroQuantity_returns400() throws Exception {
        mockMvc.perform(post("/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":10,\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /carts/{id}/items/{itemId} ---

    @Test
    void updateItem_valid_returns200() throws Exception {
        CartItemDto dto = CartItemDto.builder()
                .id(100L).productId(10L).productName("Laptop")
                .productPrice(BigDecimal.valueOf(500)).quantity(5)
                .subtotal(BigDecimal.valueOf(2500))
                .build();

        when(cartService.updateCartItem(eq(1L), eq(100L), any())).thenReturn(dto);

        mockMvc.perform(put("/carts/1/items/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.subtotal").value(2500));
    }

    @Test
    void updateItem_zeroQuantity_returns400() throws Exception {
        mockMvc.perform(put("/carts/1/items/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /carts/{id}/items/{itemId} ---

    @Test
    void removeItem_returns204() throws Exception {
        doNothing().when(cartService).removeItemFromCart(1L, 100L);

        mockMvc.perform(delete("/carts/1/items/100"))
                .andExpect(status().isNoContent());
    }

    // --- DELETE /carts/{id} ---

    @Test
    void deleteCart_returns204() throws Exception {
        doNothing().when(cartService).deleteCart(1L);

        mockMvc.perform(delete("/carts/1"))
                .andExpect(status().isNoContent());
    }
}
