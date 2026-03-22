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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    // --- createCart ---

    @Test
    void createCart_returnsCartDto() {
        Cart saved = Cart.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        CartDto result = cartService.createCart();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getCart ---

    @Test
    void getCart_found_returnsCartDtoWithTotal() {
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        Cart cart = Cart.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();
        cart.getItems().add(item);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartDto result = cartService.getCart(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("Laptop");
        assertThat(result.getItems().get(0).getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void getCart_notFound_throwsResourceNotFoundException() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- addItemToCart ---

    @Test
    void addItemToCart_newProduct_returnsCartItemDto() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        AddItemToCartRequest request = new AddItemToCartRequest(10L, 2);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        CartItem savedItem = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemDto result = cartService.addItemToCart(1L, request);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getProductName()).isEqualTo("Laptop");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void addItemToCart_existingProduct_incrementsQuantity() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        CartItem existingItem = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();

        AddItemToCartRequest request = new AddItemToCartRequest(10L, 3);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingItem));

        CartItem savedItem = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(5)
                .build();
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemDto result = cartService.addItemToCart(1L, request);

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }

    @Test
    void addItemToCart_productNotFound_throwsResourceNotFoundException() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        AddItemToCartRequest request = new AddItemToCartRequest(99L, 1);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addItemToCart_cartNotFound_throwsResourceNotFoundException() {
        AddItemToCartRequest request = new AddItemToCartRequest(10L, 1);

        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- updateCartItem ---

    @Test
    void updateCartItem_ok_returnsUpdatedCartItemDto() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        CartItem item = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        CartItem savedItem = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(5)
                .build();
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemDto result = cartService.updateCartItem(1L, 100L, request);

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }

    @Test
    void updateCartItem_itemNotBelongToCart_throwsResourceNotFoundException() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        CartItem item = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateCartItem(999L, 100L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("does not belong");
    }

    // --- removeItemFromCart ---

    @Test
    void removeItemFromCart_ok_deletesItem() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        Product product = Product.builder()
                .id(10L).name("Laptop").price(BigDecimal.valueOf(500)).stockQuantity(5)
                .build();

        CartItem item = CartItem.builder()
                .id(100L).cart(cart).product(product).quantity(2)
                .build();

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        cartService.removeItemFromCart(1L, 100L);

        verify(cartItemRepository).delete(item);
    }

    // --- deleteCart ---

    @Test
    void deleteCart_ok_deletesCart() {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        cartService.deleteCart(1L);

        verify(cartRepository).delete(cart);
    }

    @Test
    void deleteCart_notFound_throwsResourceNotFoundException() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCart(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
