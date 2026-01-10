package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "customers")
public class Customer {
    @Id
    Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    User user;
    String address;
    Long point;
}
