package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    User findByEmailIgnoreCase(String email);

    User findByEmailIgnoreCaseAndStatus(String email, boolean status);

    @EntityGraph(attributePaths = { "role", "role.permissions", "staff" })
    @Query("select u from User u where lower(u.email) = lower(:email)")
    User findByEmailWithRolePermissions(@Param("email") String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query(value = "select count(*) from customers c join users u on u.id = c.user_id", nativeQuery = true)
    Integer countCustomer();
}
