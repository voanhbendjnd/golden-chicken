package vn.edu.fpt.golden_chicken.domain.entity;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Entity
@Data
@Table(name = "products")
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE products SET is_delete = 1 WHERE id = ?")
@Where(clause = "is_delete = 0")
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
    @Column(name = "is_delete")
    Boolean isDelete;
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
    Integer sold;
    @OneToMany(mappedBy = "product")
    List<CartItem> cartItems;
}
