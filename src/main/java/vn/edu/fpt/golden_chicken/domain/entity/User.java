package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String password;
    @Column(name = "full_name")
    String fullName;
    String email;
    Boolean status;
    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    Customer customer;
    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    Staff staff;
    String phone;
    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;
}
