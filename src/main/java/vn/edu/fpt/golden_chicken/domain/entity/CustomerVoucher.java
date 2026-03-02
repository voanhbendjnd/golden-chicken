package vn.edu.fpt.golden_chicken.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
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
    LocalDateTime usedAt;
    LocalDateTime redeemedAt;
    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

    // @PrePersist
    // public void handleBeforeCreateAt() {
    // this.usedAt = LocalDateTime.now();

    // }
    @PrePersist
    public void handleBeforeCreateAt() {
        this.redeemedAt = LocalDateTime.now();
    }
}
