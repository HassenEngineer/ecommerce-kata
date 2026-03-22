package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDto> items;
    private BigDecimal total;
}
