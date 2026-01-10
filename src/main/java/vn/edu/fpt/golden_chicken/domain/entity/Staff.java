package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.StaffStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Entity
@Table(name = "staffs")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Staff {
    @Id
    Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    User user;
    @Enumerated(EnumType.STRING)
    StaffStatus status;
    @Enumerated(EnumType.STRING)
    StaffType staffType;
}
