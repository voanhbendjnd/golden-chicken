package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "voucher")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;
//Nvarchar đang lỗi tiếng việt
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer discountValue;

    @Column(nullable = false, length = 20)
    private String discountType; // PERCENT / FIXED

    private BigDecimal minOrderValue;

    private Integer pointCost;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE / DISABLED / EXPIRED

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private boolean exchangeable = true;


    public Voucher() {}


}


