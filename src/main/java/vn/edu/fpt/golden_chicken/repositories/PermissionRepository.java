package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findAllByModule(String name);

    boolean existsByNameAndApiPathAndMethodAndModule(String name, String apiPath, String method, String module);

    boolean existsByNameAndApiPathAndMethodAndModuleAndIdNot(String name, String apiPath, String method, String module,
            long id);
}
