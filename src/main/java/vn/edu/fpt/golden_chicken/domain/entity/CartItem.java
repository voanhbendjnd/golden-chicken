package vn.edu.fpt.golden_chicken.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.DialectOverride.Version;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cart_items")
public class CartItem {
    Long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
    Integer quantity;
    BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
    LocalDateTime createdAt, updatedAt;

    @PrePersist
    public void handleBeforeCreateAt() {
        this.createdAt = LocalDateTime.now();

    }

    @PreUpdate
    public void handleBeforeUpdateBy() {
        this.updatedAt = LocalDateTime.now();

    }
}
