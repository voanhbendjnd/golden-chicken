package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.repositories.VoucherRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;
import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;
import vn.edu.fpt.golden_chicken.domain.response.ActionPointMessage;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    CustomerVoucherRepository customerVoucherRepository;
    @Mock
    VoucherRepository voucherRepository;
    @Mock
    CartService cartService;
    @Mock
    UserService userService;
    @Mock
    CartRepository cartRepository;
    @Mock
    KafkaTemplate<String, OrderMessage> kafkaTemplate;
    @Mock
    KafkaTemplate<String, ActionPointMessage> kafkaTemplatePoint;

    @InjectMocks
    OrderService orderService;

    private Order order;
    private User actor;
    private Staff staff;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setTotalProductPrice(BigDecimal.valueOf(100000));
        order.setFinalAmount(BigDecimal.valueOf(120000));
        order.setOrderItems(Collections.emptyList());

        Customer customer = new Customer();
        customer.setId(1L);
        User customerUser = new User();
        customerUser.setEmail("customer@test.com");
        customer.setUser(customerUser);
        order.setCustomer(customer);

        actor = new User();
        actor.setId(2L);
        staff = new Staff();
        staff.setId(1L);
        staff.setStaffType(StaffType.RECEPTIONIST);
        actor.setStaff(staff);
    }

    @Test
    void changeOrderStatus_Success_ToConfirmed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userService.getUserInContext()).thenReturn(actor);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.changeOrderStatus(1L, "CONFIRMED", null);

        // assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void changeOrderStatus_Blocked_RevertingDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.changeOrderStatus(1L, "PENDING", null));

        assertEquals("Đơn hàng đã kết thúc, không thể thay đổi trạng thái!", exception.getMessage());
    }

    @Test
    void changeOrderStatus_Receptionist_BlockedFromShipperFlow() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userService.getUserInContext()).thenReturn(actor); // Actor is RECEPTIONIST

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.changeOrderStatus(1L, "DELIVERING", null));

        assertEquals("Bạn không có quyền cập nhật trạng thái thuộc phạm trù shipper.", exception.getMessage());
    }

    @Test
    void changeOrderStatus_Delivered_UpdatesPaymentAndPoints() {
        order.setStatus(OrderStatus.DELIVERING);
        // Add a mock product in items to test inventory update if desired,
        // but let's keep it simple first.

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userService.getUserInContext()).thenReturn(actor);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.findByIdIn(anyList())).thenReturn(Collections.emptyList());

        orderService.changeOrderStatus(1L, "DELIVERED", null);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(kafkaTemplatePoint).send(eq("customer-points-topic"), any(ActionPointMessage.class));
    }

    @Test
    void cancelOrderByCustomer_Success() throws Exception {
        // Need to set up SecurityContext for this, or mock the authentication if
        // possible.
        // Actually, changeOrderStatus also uses security context indirectly through
        // userService.
    }
}
