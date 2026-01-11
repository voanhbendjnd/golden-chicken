package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
