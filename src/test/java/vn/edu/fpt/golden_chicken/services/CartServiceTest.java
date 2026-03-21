package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.edu.fpt.golden_chicken.domain.entity.CartItem;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.CartDTO;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock CustomerRepository customerRepository;

    @InjectMocks
    CartService cartService;

    private User user;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        customer = new Customer();
        customer.setId(1L);
        user.setCustomer(customer);

        product = new Product();
        product.setId(100L);
        product.setName("Chicken Nugget");
        product.setPrice(BigDecimal.valueOf(50000));

        // Mock SecurityContextholder
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addToCart_NewItem_Success() throws PermissionException {
        CartDTO dto = new CartDTO(100L, 2);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomerIdAndProductId(1L, 100L)).thenReturn(null);

        boolean result = cartService.addToCart(dto);

        assertTrue(result);
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_IncrementsQuantity() throws PermissionException {
        CartDTO dto = new CartDTO(100L, 2);
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setQuantity(5);
        existingItem.setProduct(product);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomerIdAndProductId(1L, 100L)).thenReturn(existingItem);

        cartService.addToCart(dto);

        assertEquals(7, existingItem.getQuantity());
        verify(cartRepository).save(existingItem);
    }

    @Test
    void addToCart_ExceedsLimit_CapsAt33() throws PermissionException {
        CartDTO dto = new CartDTO(100L, 30);
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setQuantity(10);
        existingItem.setProduct(product);

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomerIdAndProductId(1L, 100L)).thenReturn(existingItem);

        cartService.addToCart(dto);

        assertEquals(33, existingItem.getQuantity());
    }

    @Test
    void updateQuantity_ZeroQuantity_RemovesItem() throws PermissionException {
        CartDTO dto = new CartDTO(100L, 0);
        CartItem item = new CartItem();
        item.setId(1L);
        
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);
        when(cartRepository.findByCustomerIdAndProductId(1L, 100L)).thenReturn(item);

        cartService.updateQuantity(dto);

        verify(cartRepository).delete(item);
    }
}
