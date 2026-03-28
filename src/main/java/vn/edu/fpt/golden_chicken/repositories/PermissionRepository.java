package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import vn.edu.fpt.golden_chicken.domain.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findAllByModule(String name);

    boolean existsByNameAndApiPathAndMethodAndModule(String name, String apiPath, String method, String module);

    boolean existsByNameAndApiPathAndMethodAndModuleAndIdNot(String name, String apiPath, String method, String module,
            long id);

    List<Permission> findByIdIn(List<Long> ids);

    boolean existsByApiPathAndMethod(String api, String method);

    @Query("SELECT COUNT(r) > 0 FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    boolean isPermissionInUse(@Param("permissionId") Long permissionId);

}
