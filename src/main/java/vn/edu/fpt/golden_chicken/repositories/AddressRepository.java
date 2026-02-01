package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserIdAndStatusOrderByIsDefaultDescIdDesc(Long userId, String status);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findFirstByUserIdAndStatusAndIsDefaultTrue(Long userId, String status);

    List<Address> findAllByUserIdAndStatusAndIsDefaultTrue(Long userId, String status);
}
