package com.miniproject.minidmart.service;

import com.miniproject.minidmart.entity.Cart;
import com.miniproject.minidmart.entity.CartItem;
import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.CartItemRepository;
import com.miniproject.minidmart.repository.CartRepository;
import com.miniproject.minidmart.repository.ProductRepository;
import com.miniproject.minidmart.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    // Get customer's cart
    public Cart getCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found with id: " + userId
                                    ));

                    Cart cart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(cart);
                });
    }


    // Add product to cart
    public Cart addToCart(
            Long userId,
            Long productId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: " + productId
                        ));

        if (!product.getActive()) {
            throw new RuntimeException("Product is inactive");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() ->
                        cartRepository.save(
                                Cart.builder()
                                        .user(user)
                                        .build()
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException(
                        "Insufficient stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
        }

        cartItemRepository.save(cartItem);

        return cartRepository.findById(cart.getId())
                .orElseThrow();
    }


    // Update cart item quantity
    public Cart updateQuantity(
            Long userId,
            Long productId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found in cart"
                                ));

        Product product = cartItem.getProduct();

        if (!product.getActive()) {
            throw new RuntimeException(
                    "Product is inactive"
            );
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock"
            );
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return cart;
    }


    // Remove product from cart
    public Cart removeFromCart(
            Long userId,
            Long productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found in cart"
                                ));

        cartItemRepository.delete(cartItem);

        return cart;
    }


    // Clear cart
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        cart.getItems().clear();

        cartRepository.save(cart);
    }
}