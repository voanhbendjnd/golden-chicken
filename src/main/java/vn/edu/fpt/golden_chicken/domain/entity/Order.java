package vn.edu.fpt.golden_chicken.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;

@Data
@Entity
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
    LocalDateTime updatedAt;
    LocalDateTime createdAt;
    BigDecimal totalProductPrice;
    BigDecimal shippingFee;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String phone;
    @Column(columnDefinition = "NVARCHAR(255)")

    String name;
    // Long bonus;
    @Enumerated(EnumType.STRING)
    OrderStatus status;
    @Column(columnDefinition = "NVARCHAR(255)")
    @Enumerated(EnumType.STRING)

    PaymentStatus paymentStatus;
    @Column(columnDefinition = "NVARCHAR(255)")

    String shippingAddress;
    @Column(columnDefinition = "NVARCHAR(255)")

    String note;
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> orderItems;

    @PrePersist
    public void handleBeforeCreateAt() {
        this.createdAt = LocalDateTime.now();

    }

    @PreUpdate
    public void handleBeforeUpdateBy() {
        this.updatedAt = LocalDateTime.now();
    }
}
