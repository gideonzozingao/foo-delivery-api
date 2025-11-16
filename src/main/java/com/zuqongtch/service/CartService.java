package com.zuqongtch.service;

import com.zuqongtch.dto.cart.AddToCartRequest;
import com.zuqongtch.dto.cart.CartResponse;
import com.zuqongtch.entity.Cart;
import com.zuqongtch.entity.CartItem;
import com.zuqongtch.entity.MenuItem;
import com.zuqongtch.entity.User;
import com.zuqongtch.exception.ResourceNotFoundException;
import com.zuqongtch.repository.CartItemRepository;
import com.zuqongtch.repository.CartRepository;
import com.zuqongtch.repository.MenuItemRepository;
import com.zuqongtch.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User currentUser = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getAvailable()) {
            throw new IllegalStateException("Menu item is not available");
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository
                .findByCartAndMenuItem(cart, menuItem)
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setSpecialInstructions(request.getSpecialInstructions());
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .price(menuItem.getPrice())
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cartItemRepository.save(cartItem);
        }

        // Refresh cart
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(Long cartItemId, Integer quantity) {
        User currentUser = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Verify cart item belongs to current user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalStateException("Cart item does not belong to your cart");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        // Refresh cart
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long cartItemId) {
        User currentUser = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Verify cart item belongs to current user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalStateException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);

        // Refresh cart
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        User currentUser = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        cartItemRepository.deleteAll(cart.getItems());
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartResponse.CartItemDto> items = cart.getItems().stream()
                .map(item -> CartResponse.CartItemDto.builder()
                        .cartItemId(item.getId())
                        .menuItemId(item.getMenuItem().getId())
                        .menuItemName(item.getMenuItem().getName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .itemTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .specialInstructions(item.getSpecialInstructions())
                        .imageUrl(item.getMenuItem().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartResponse.CartItemDto::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(CartResponse.CartItemDto::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .totalItems(totalItems)
                .build();
    }
}