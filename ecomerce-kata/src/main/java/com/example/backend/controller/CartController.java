package com.example.backend.controller;


import com.example.backend.dto.AddItemToCartRequest;
import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.dto.UpdateCartItemRequest;
import com.example.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartDto createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{cartId}")
    public CartDto getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemDto addItem(@PathVariable Long cartId,
                               @Valid @RequestBody AddItemToCartRequest request) {
        return cartService.addItemToCart(cartId, request);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public CartItemDto updateItem(@PathVariable Long cartId,
                                  @PathVariable Long itemId,
                                  @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateCartItem(cartId, itemId, request);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long cartId, @PathVariable Long itemId) {
        cartService.removeItemFromCart(cartId, itemId);
    }

    @DeleteMapping("/{cartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
    }
}
