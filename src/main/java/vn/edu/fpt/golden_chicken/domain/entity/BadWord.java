package vn.edu.fpt.golden_chicken.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "bad_words")
public class BadWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String word;
    Boolean status;
}
