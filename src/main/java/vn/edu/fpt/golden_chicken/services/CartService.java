package vn.edu.fpt.golden_chicken.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.CartItem;
import vn.edu.fpt.golden_chicken.domain.request.CartDTO;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
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
}
