package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.CartItem;
import vn.edu.fpt.golden_chicken.domain.request.CartDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartService {
    UserRepository userRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;

    @Transactional
    public void addToCart(CartDTO dto) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User Email", email);
        }
        if (user.getCustomer() == null) {
            throw new PermissionException("Only customers can perform this action!");
        }
        var customer = user.getCustomer();
        var product = this.productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", dto.productId()));
        var cart = this.cartRepository.findByCustomerId(customer.getId());
        var cartItem = this.cartRepository.findByCustomerIdAndProductId(customer.getId(), product.getId());
        if (cartItem == null) {
            cartItem = new CartItem();
        }
        if (cartItem.getId() == null) {
            cartItem.setProduct(product);
            cartItem.setQuantity(dto.quantity());
            cartItem.setPrice(product.getPrice());
            customer.addCartItem(cartItem);
        } else {
            int currentQty = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;

            cartItem.setQuantity(currentQty + dto.quantity());
            cartItem.setPrice(product.getPrice());
        }
        this.cartRepository.save(cartItem);

    }

    public CartResponse getProductInCart() throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null && !email.isEmpty()) {
            var user = this.userRepository.findByEmail(email);
            if (user != null) {
                var customer = user.getCustomer();
                if (customer != null) {
                    List<CartItem> cart = customer.getCartItems();
                    var res = new CartResponse();
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    int totalQty = cart.size();
                    var cartItems = new ArrayList<CartResponse.CartItemDTO>();
                    var products = cart.stream().map(x -> x.getProduct())
                            .collect(Collectors.toMap(x -> x.getId(), x -> x));
                    if (products.size() == 0) {
                        res.setItems(cartItems);
                        res.setTotalPrice(totalPrice);
                        res.setTotalQuantity(totalQty);
                        return res;
                    }
                    for (var x : cart) {
                        var cartItem = new CartResponse.CartItemDTO();
                        var product = products.get(x.getProduct().getId());
                        if (product != null) {
                            cartItem.setPrice(product.getPrice());
                            cartItem.setProductId(product.getId());
                            cartItem.setProductImg(product.getImageUrl());
                            cartItem.setQuantity(x.getQuantity());
                            BigDecimal lastPriceProduct = product.getPrice().multiply(new BigDecimal(x.getQuantity()));
                            cartItem.setSubTotal(lastPriceProduct);
                            cartItem.setPrice(x.getPrice());
                            totalPrice = totalPrice.add(lastPriceProduct);
                            cartItems.add(cartItem);
                        } else {
                            throw new PermissionException("Product Not Found!");

                        }

                    }
                    res.setItems(cartItems);
                    res.setTotalPrice(totalPrice);
                    res.setTotalQuantity(totalQty);
                    return res;
                }
                throw new PermissionException("You do not have permission!");
            }
            throw new ResourceNotFoundException("User Email", email);
        }
        throw new PermissionException("You must be login for this service!");
    }

    public void updateQuantity(CartDTO dto) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User Email", email);
        }
        if (user.getCustomer() == null) {
            throw new PermissionException("Only customers can perform this action!");
        }
        var customer = user.getCustomer();
        var cart = this.cartRepository.findByCustomerId(customer.getId());
        var cartItem = this.cartRepository.findByCustomerIdAndProductId(customer.getId(), dto.productId());
        if (cartItem == null) {
            cartItem = new CartItem();
        }
        if (dto.quantity() <= 0) {
            if (cartItem != null) {
                this.cartRepository.delete(cartItem);
            }
            return;
        }
        if (cartItem == null) {
            var product = this.productRepository.findById(dto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product ID", dto.productId()));
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCustomer(customer);
            cartItem.setQuantity(dto.quantity());
            cartItem.setPrice(product.getPrice());
        } else {
            cartItem.setQuantity(dto.quantity());
            cartItem.setPrice(cartItem.getProduct().getPrice());
        }
        this.cartRepository.save(cartItem);
    }
}
