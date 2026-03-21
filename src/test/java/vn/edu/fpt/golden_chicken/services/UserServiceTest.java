package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.RegisterDTO;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock CustomerRepository customerRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void register_Success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("new@test.com");
        dto.setName("New User");
        dto.setPassword("password123");

        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(new Role());

        userService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("existing@test.com");

        when(userRepository.existsByEmailIgnoreCase("existing@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(dto));
    }

    @Test
    void getMaskedCustomerName_Tests() {
        assertEquals("Khách ẩn danh", userService.getMaskedCustomerName(null));
        assertEquals("Khách ẩn danh", userService.getMaskedCustomerName("   "));
        assertEquals("L***h", userService.getMaskedCustomerName("Linh"));
        assertEquals("V***i", userService.getMaskedCustomerName("Vo Anh"));
        assertEquals("A***", userService.getMaskedCustomerName("An"));
    }

    @Test
    void checkLockedAccount_Locked_ReturnsFalse() {
        User user = new User();
        Customer customer = new Customer();
        customer.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        user.setCustomer(customer);

        when(userRepository.findByEmailIgnoreCaseAndStatus(anyString(), eq(true))).thenReturn(user);

        assertFalse(userService.checkLockedAccount("locked@test.com"));
    }

    @Test
    void checkLockedAccount_NotLocked_ReturnsTrue() {
        User user = new User();
        Customer customer = new Customer();
        customer.setLockedUntil(null);
        user.setCustomer(customer);

        when(userRepository.findByEmailIgnoreCaseAndStatus(anyString(), eq(true))).thenReturn(user);

        assertTrue(userService.checkLockedAccount("active@test.com"));
    }
}
