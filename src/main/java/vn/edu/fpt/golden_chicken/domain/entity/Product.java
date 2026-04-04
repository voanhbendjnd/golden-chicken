package vn.edu.fpt.golden_chicken.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Entity
@Getter
@Setter
@Table(name = "products")
@FieldDefaults(level = AccessLevel.PRIVATE)
// @SQLDelete(sql = "UPDATE products SET is_delete = 1 WHERE id = ?")
// @Where(clause = "is_delete = 0")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String name;
    BigDecimal price;
    // @Column(columnDefinition = "MEDIUMTEXT")
    @Column(columnDefinition = "NVARCHAR(255)")
    String description;
    Boolean active;
    String imageUrl;
    @Enumerated(EnumType.STRING)
    ProductType type;
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProductImage> productImages;
    @OneToMany(mappedBy = "combo")
    List<ComboDetail> comboDetails;
    @OneToMany(mappedBy = "product")
    List<ComboDetail> productDetails;
    @OneToMany(mappedBy = "product")
    List<OrderItem> orderItems;
    Integer sold = 0;
    @OneToMany(mappedBy = "product")
    List<CartItem> cartItems;
    @OneToMany(mappedBy = "product")
    List<Review> reviews;
    LocalDateTime createdAt, updatedAt;
    Double averageRating = 0.0;
    Integer totalReviews = 0;

    @PrePersist
    public void handleBeforeCreateAt() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    }

    @PreUpdate
    public void handleBeforeUpdateBy() {
        this.updatedAt = LocalDateTime.now();
    }
}
