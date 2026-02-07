package vn.edu.fpt.golden_chicken.domain.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "customers")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    User user;
    @Column(columnDefinition = "NVARCHAR(255)")
    String address;
    Long point;
    @OneToMany(mappedBy = "customer")
    List<Order> orders;
}
