package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "order_voucher_histories")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class OrderVoucherHistory {
    @Id
    Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "customer_voucher_id")
    CustomerVoucher customerVoucher;
    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

}
