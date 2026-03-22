package com.example.backend.service;

import com.example.backend.dto.AddItemToCartRequest;
import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.dto.UpdateCartItemRequest;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Cart;
import com.example.backend.model.CartItem;
import com.example.backend.model.Product;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartDto createCart() {
        Cart cart = new Cart();
        return toDto(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Long cartId) {
        return toDto(findCartOrThrow(cartId));
    }

    public CartItemDto addItemToCart(Long cartId, AddItemToCartRequest request) {
        Cart cart = findCartOrThrow(cartId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + request.getProductId()));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId())
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        return toItemDto(cartItemRepository.save(item));
    }

    public CartItemDto updateCartItem(Long cartId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findCartItemOrThrow(itemId);
        if (!item.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("Cart item " + itemId + " does not belong to cart " + cartId);
        }
        item.setQuantity(request.getQuantity());
        return toItemDto(cartItemRepository.save(item));
    }

    public void removeItemFromCart(Long cartId, Long itemId) {
        CartItem item = findCartItemOrThrow(itemId);
        if (!item.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("Cart item " + itemId + " does not belong to cart " + cartId);
        }
        cartItemRepository.delete(item);
    }

    public void deleteCart(Long cartId) {
        Cart cart = findCartOrThrow(cartId);
        cartRepository.delete(cart);
    }

    private Cart findCartOrThrow(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id " + id));
    }

    private CartItem findCartItemOrThrow(Long id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id " + id));
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toItemDto)
                .toList();

        BigDecimal total = itemDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .id(cart.getId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(itemDtos)
                .total(total)
                .build();
    }

    private CartItemDto toItemDto(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
