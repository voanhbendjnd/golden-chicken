package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    @Column(columnDefinition = "NVARCHAR(255)")
    String recipientName;
    String recipientPhone;
    @Column(columnDefinition = "NVARCHAR(255)")
    String specificAddress;
    @Column(columnDefinition = "NVARCHAR(255)")
    String ward;
    @Column(columnDefinition = "NVARCHAR(255)")
    String district;
    @Column(columnDefinition = "NVARCHAR(255)")
    String city;
    Boolean isDefault = false;
    String status = "ACTIVE";
}
