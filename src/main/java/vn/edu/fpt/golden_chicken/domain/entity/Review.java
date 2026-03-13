package vn.edu.fpt.golden_chicken.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.StringListConverter;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Entity
@Setter
@Getter
@Table(name = "reviews")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    @Id
    Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    OrderItem orderItem;
    @Min(value = 1, message = "Min rating is 1")
    @Max(value = 5, message = "Max rating is 5")
    Integer rating;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String comment;
    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
    @Convert(converter = StringListConverter.class)
    @Column(name = "media_urls", columnDefinition = "NVARCHAR(MAX)")
    List<String> mediaUrls = new ArrayList<>();
    Boolean isUpdate = Boolean.FALSE;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    ReviewStatus reviewStatus = ReviewStatus.PUBLISHED;

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
