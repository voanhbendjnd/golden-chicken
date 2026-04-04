package vn.edu.fpt.golden_chicken.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "customer_vouchers")
public class CustomerVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
    @ManyToOne
    @JoinColumn(name = "voucher_id")
    Voucher voucher;
    StatusVoucher status;
    LocalDateTime redeemedAt;

    @OneToOne(mappedBy = "customerVoucher", cascade = CascadeType.ALL)
    @ToString.Exclude
    OrderVoucherHistory orderVoucherHistory;
    // @PrePersist
    // public void handleBeforeCreateAt() {
    // this.usedAt = LocalDateTime.now();

    // }
    @PrePersist
    public void handleBeforeCreateAt() {
        this.redeemedAt = LocalDateTime.now();
    }
}
