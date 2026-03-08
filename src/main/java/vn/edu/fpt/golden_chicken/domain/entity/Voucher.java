package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "voucher")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(nullable = false)
    private Integer discountValue;

    @Column(nullable = false, length = 20)
    private String discountType; // PERCENT / FIXED

    private BigDecimal minOrderValue;

    private Integer pointCost;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE / DISABLED / EXPIRED

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private Integer quantity ;

    @Column(nullable = false, length = 20)
    private String voucherType; // PRODUCT / SHIPPING

    @Column(nullable = false)
    private boolean exchangeable = true;
    @OneToMany(mappedBy = "voucher")
    private List<CustomerVoucher> customerVouchers;

    public Voucher() {
    }

}
