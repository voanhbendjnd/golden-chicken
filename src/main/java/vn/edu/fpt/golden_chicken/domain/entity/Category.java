package vn.edu.fpt.golden_chicken.domain.entity;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "categories")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE categories SET is_delete = 1 WHERE id = ?")
@Where(clause = "is_delete = 0")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String name;
    // @Column(columnDefinition = "MEDIUMTEXT")
    @Column(columnDefinition = "NVARCHAR(255)")
    String description;
    @OneToMany(mappedBy = "category")
    List<Product> products;
    Boolean status;
    @Column(name = "is_delete")
    Boolean isDelete;
}
