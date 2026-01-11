package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Staff;
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

}
