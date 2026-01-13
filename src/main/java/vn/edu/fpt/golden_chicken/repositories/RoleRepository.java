package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByNameAndIdNot(String name, long id);

    Role findByName(String name);

    boolean existsByName(String name);

}
