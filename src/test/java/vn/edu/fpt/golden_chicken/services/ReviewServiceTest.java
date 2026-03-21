package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.ReviewDTO;
import vn.edu.fpt.golden_chicken.domain.response.ReviewMessage;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderItemRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;
import vn.edu.fpt.golden_chicken.utils.BadWordFilterUtility;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    UserService userService;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    BadWordFilterUtility badWordFilterUtility;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    KafkaTemplate<String, ReviewMessage> kafkaReviewTemplate;
    @Mock
    KafkaTemplate<String, String> kafkaBanViolate;

    @InjectMocks
    ReviewService reviewService;

    private Customer customer;
    private OrderItem orderItem;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        User user = new User();
        user.setEmail("test@example.com");
        customer.setUser(user);

        product = new Product();
        product.setId(10L);
        product.setName("Chicken");

        Order order = new Order();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCustomer(customer);

        orderItem = new OrderItem();
        orderItem.setId(100L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
    }

    @Test
    void saveReview_Success_NoBadWords() throws Exception {
        ReviewDTO dto = new ReviewDTO();
        dto.setComment("Delicious!");
        dto.setRating(5);

        when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));
        when(badWordFilterUtility.isViolating(anyString())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = reviewService.saveReview(dto, customer, null, 100L);

        assertTrue(result.startsWith("success_"));
        verify(reviewRepository).save(argThat(r -> r.getReviewStatus() == null)); // Default status
    }

    @Test
    void saveReview_ViolatesBadWord_Rejected() throws Exception {
        ReviewDTO dto = new ReviewDTO();
        dto.setComment("Baddddd word!");
        dto.setRating(1);

        when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));
        when(badWordFilterUtility.isViolating(eq("Baddddd word!"))).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = reviewService.saveReview(dto, customer, null, 100L);

        assertTrue(result.startsWith("fail_"));
        verify(reviewRepository).save(argThat(r -> r.getReviewStatus() == ReviewStatus.REJECTED));
        assertEquals(1, customer.getViolationCount());
    }

    @Test
    void saveReview_FifthViolation_LocksAccount() throws Exception {
        customer.setViolationCount(4);
        ReviewDTO dto = new ReviewDTO();
        dto.setComment("Baddddd word!");

        when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));
        when(badWordFilterUtility.isViolating(anyString())).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        reviewService.saveReview(dto, customer, null, 100L);

        assertNotNull(customer.getLockedUntil());
        verify(kafkaBanViolate).send(eq("violate-account-topic"), eq("test@example.com"));
        verify(userService).forceLogoutCurrentUser();
    }
}
