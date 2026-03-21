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
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.CheckoutException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@SuppressWarnings("unusend")
public class CartService {
    CartRepository cartRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    vn.edu.fpt.golden_chicken.repositories.CustomerRepository customerRepository;

    @Transactional
    public void addToCart(Long customerId, Long productId, Integer quantity) {
        var customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer ID", customerId));
        var product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", productId));

        var cartItem = this.cartRepository.findByCustomerIdAndProductId(customerId, productId);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            customer.addCartItem(cartItem);
        } else {
            int currentQty = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
            cartItem.setQuantity(currentQty + quantity);
            cartItem.setPrice(product.getPrice());
        }
        this.cartRepository.save(cartItem);
    }

    @Transactional
    public boolean addToCart(CartDTO dto) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = this.userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            return false;
            // throw new ResourceNotFoundException("User Email", email);
        }
        if (user.getCustomer() == null) {
            return false;
            // throw new PermissionException("Only customers can perform this action!");
        }
        var customer = user.getCustomer();
        var product = this.productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product ID", dto.productId()));
        // var cart = this.cartRepository.findByCustomerId(customer.getId());
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
        return true;
    }

    public CartResponse getProductInCart() throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null && !email.isEmpty()) {
            var user = this.userRepository.findByEmailIgnoreCase(email);
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
                            cartItem.setItemId(x.getId());
                            cartItem.setProductName(product.getName());
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

    @Transactional
    public void updateQuantity(CartDTO dto) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = this.userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new ResourceNotFoundException("User Email", email);
        }
        if (user.getCustomer() == null) {
            throw new PermissionException("Only customers can perform this action!");
        }
        var customer = user.getCustomer();
        // var cart = this.cartRepository.findByCustomerId(customer.getId());
        var cartItem = this.cartRepository.findByCustomerIdAndProductId(customer.getId(), dto.productId());
        if (dto.quantity() <= 0) {
            if (cartItem != null) {
                customer.getCartItems().removeIf(item -> item.getId().equals(cartItem.getId()));
                this.cartRepository.delete(cartItem);
            }
            return;
        }
        if (cartItem == null) {
            var product = this.productRepository.findById(dto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product ID", dto.productId()));
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

    public Integer sumCart() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.isEmpty()) {
            throw new ResourceNotFoundException("User Email", email);
        }
        var user = this.userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            return null;
            // throw new ResourceNotFoundException("User Email", email);
        }
        var customer = user.getCustomer();
        if (customer == null) {
            return null;
        }
        var cartItems = customer.getCartItems();
        if (cartItems == null) {
            return 0;
        } else {
            return cartItems.size();
        }
    }

    public void cleanCartAfterCheckout(List<OrderDTO.OrderDetail> item) {
        if (item.isEmpty()) {
            return;
        }
        var listFromCart = new ArrayList<CartItem>();
        if (item.getFirst().getItemId() != null) {
            var cartItems = this.cartRepository
                    .findByIdIn(item.stream().map(x -> x.getItemId()).collect(Collectors.toList()));
            var mpCartItems = item.stream()
                    .collect(Collectors.toMap(x -> x.getItemId(), x -> x));
            for (var x : cartItems) {
                var qtyInCart = x.getQuantity();
                if (qtyInCart <= 0) {
                    throw new CheckoutException("Quantity Product with Name (" + x.getProduct().getName()
                            + ") in cart less than or equal 0!");
                }
                var it = mpCartItems.get(x.getId());
                if (it != null) {
                    var lastQty = qtyInCart - it.getQuantity();
                    if (lastQty < 0) {
                        throw new CheckoutException("Quantity Product with Name (" + x.getProduct().getName()
                                + ") in cart less than or equal 0!");

                    } else if (lastQty == 0) {
                        listFromCart.add(x);
                    } else if (lastQty > 0) {
                        x.setQuantity(lastQty);
                    }

                } else {
                    throw new ResourceNotFoundException("Cart Item ID", x.getId());
                }
            }
            this.cartRepository.saveAll(cartItems);
            if (!listFromCart.isEmpty() || listFromCart.size() != 0) {
                this.cartRepository.deleteAll(listFromCart);
            }
        }
    }
}
