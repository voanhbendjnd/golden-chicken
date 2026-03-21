package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.fpt.golden_chicken.domain.entity.Address;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    ProductService productService;
    @Mock
    CartService cartService;
    @Mock
    OrderService orderService;
    @Mock
    AddressServices addressServices;
    @Mock
    VoucherService voucherService;
    @Mock
    ProfileService profileService;
    @Mock
    ShippingFeeService shippingFeeService;

    @InjectMocks
    CheckoutService checkoutService;

    private User currentUser;
    private Address defaultAddress;
    private ResProduct product;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        Customer customer = new Customer();
        customer.setId(1L);
        currentUser.setCustomer(customer);

        defaultAddress = new Address();
        defaultAddress.setId(1L);
        defaultAddress.setRecipientName("Test User");
        defaultAddress.setRecipientPhone("0123456789");
        defaultAddress.setSpecificAddress("123 Street");
        defaultAddress.setWard("Ward 1");
        defaultAddress.setCity("City 1");

        product = new ResProduct();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(100000));
        product.setName("Fried Chicken");
    }

    @Test
    void buildCheckout_DirectBuy_Success() throws PermissionException {
        when(productService.findById(1L)).thenReturn(product);
        // when(addressServices.getDefaultAddress()).thenReturn(defaultAddress);
        when(shippingFeeService.getFeeByWard(anyString())).thenReturn(BigDecimal.valueOf(20000));
        when(profileService.getCurrentUser()).thenReturn(currentUser);
        when(voucherService.getCustomerVouchers(anyLong())).thenReturn(Collections.emptyList());

        CheckoutResponse response = checkoutService.buildCheckout(1L, null, null, null, null, null, 1);

        assertNotNull(response);
        OrderDTO order = (OrderDTO) response.getModel().get("order");
        assertEquals(BigDecimal.valueOf(100000), order.getTotalProductPrice());
        assertEquals(BigDecimal.valueOf(20000), order.getShippingFee());
        assertEquals(BigDecimal.valueOf(120000), order.getFinalAmount());
    }

    @Test
    void buildCheckout_WithProductVoucherPercentage_Success() throws PermissionException {
        when(productService.findById(1L)).thenReturn(product);
        // when(addressServices.getDefaultAddress()).thenReturn(defaultAddress);
        when(shippingFeeService.getFeeByWard(anyString())).thenReturn(BigDecimal.valueOf(20000));
        when(profileService.getCurrentUser()).thenReturn(currentUser);

        Voucher voucher = new Voucher();
        voucher.setId(1L);
        voucher.setDiscountType("PERCENT");
        voucher.setDiscountValue(10); // 10%
        voucher.setMinOrderValue(BigDecimal.valueOf(50000));

        CustomerVoucher cv = new CustomerVoucher();
        cv.setId(10L);
        cv.setVoucher(voucher);

        when(voucherService.getCustomerVoucherById(10L)).thenReturn(cv);

        CheckoutResponse response = checkoutService.buildCheckout(1L, null, null, 10L, null, null, 2);

        OrderDTO order = (OrderDTO) response.getModel().get("order");
        // Product 100k * 2 = 200k. 10% discount = 20k.
        assertEquals(BigDecimal.valueOf(200000), order.getTotalProductPrice());
        // assertEquals(BigDecimal.valueOf(20000).setScale(2),
        // order.getProductDiscountAmount()); // Need to check rounding
        assertTrue(BigDecimal.valueOf(20000).compareTo(order.getProductDiscountAmount()) == 0);
        assertEquals(BigDecimal.valueOf(220000).subtract(order.getProductDiscountAmount()), order.getFinalAmount());
    }

    @Test
    void buildCheckout_WithFixedVoucher_Success() throws PermissionException {
        when(productService.findById(1L)).thenReturn(product);
        // when(addressServices.getDefaultAddress()).thenReturn(defaultAddress);
        when(shippingFeeService.getFeeByWard(anyString())).thenReturn(BigDecimal.valueOf(20000));
        when(profileService.getCurrentUser()).thenReturn(currentUser);

        Voucher voucher = new Voucher();
        voucher.setId(2L);
        voucher.setDiscountType("FIXED");
        voucher.setDiscountValue(30000); // 30k discount
        voucher.setMinOrderValue(BigDecimal.valueOf(50000));

        CustomerVoucher cv = new CustomerVoucher();
        cv.setId(20L);
        cv.setVoucher(voucher);

        when(voucherService.getCustomerVoucherById(20L)).thenReturn(cv);

        CheckoutResponse response = checkoutService.buildCheckout(1L, null, null, 20L, null, null, 1);

        OrderDTO order = (OrderDTO) response.getModel().get("order");
        assertEquals(BigDecimal.valueOf(30000), order.getProductDiscountAmount());
        assertEquals(BigDecimal.valueOf(100000 + 20000 - 30000), order.getFinalAmount());
    }

    @Test
    void buildCheckout_VoucherMinOrderNotMet_Error() throws PermissionException {
        when(productService.findById(1L)).thenReturn(product);
        // when(addressServices.getDefaultAddress()).thenReturn(defaultAddress);
        when(shippingFeeService.getFeeByWard(anyString())).thenReturn(BigDecimal.valueOf(20000));
        when(profileService.getCurrentUser()).thenReturn(currentUser);

        Voucher voucher = new Voucher();
        voucher.setId(3L);
        voucher.setMinOrderValue(BigDecimal.valueOf(500000)); // Min 500k

        CustomerVoucher cv = new CustomerVoucher();
        cv.setVoucher(voucher);

        when(voucherService.getCustomerVoucherById(30L)).thenReturn(cv);

        CheckoutResponse response = checkoutService.buildCheckout(1L, null, null, 30L, null, null, 1);

        assertEquals("Đơn hàng chưa đủ điều kiện để dùng voucher này", response.getModel().get("voucherError"));
        OrderDTO order = (OrderDTO) response.getModel().get("order");
        assertEquals(BigDecimal.ZERO, order.getDiscountAmount());
    }
}
