package vn.edu.fpt.golden_chicken.config;

import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Permission;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.repositories.PermissionRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StaffStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseIntializer implements CommandLineRunner {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE <<<");
        long roleCnt = this.roleRepository.count();
        long userCnt = this.userRepository.count();
        long permissionCnt = this.permissionRepository.count();
        if (permissionCnt == 0) {
            var permissions = new ArrayList<Permission>();
            permissions.add(new Permission("ADMIN DASHBOARD", "/admin", "GET", "DASHBOARD ADMIN"));
            permissions.add(new Permission("IMPORT USER", "/admin/user/import", "POST", "USERS"));
            permissions.add(new Permission("USER TABLE PAGE", "/admin/user", "GET", "USERS"));
            permissions.add(new Permission("CREATE USER", "/admin/user/create", "POST", "USERS"));
            permissions.add(new Permission("CREATE USER", "/admin/user/create", "GET", "USERS"));
            permissions.add(new Permission("UPDATE USER", "/admin/user/update", "POST", "USERS"));
            permissions.add(new Permission("UPDATE USER", "/admin/user/update/{id}", "GET", "USERS"));
            permissions.add(new Permission("DELETE USER", "/admin/user/{id}", "POST", "USERS"));
            permissions.add(new Permission("FIND USER BY ID", "/admin/user/{id}", "GET", "USERS"));
            permissions.add(new Permission("DELETE USER POST", "/admin/user/delete/{id}", "GET", "USERS"));

            permissions.add(new Permission("CREATE ROLE", "/admin/role", "POST", "ROLES"));
            permissions.add(new Permission("CREATE ROLE", "/admin/role/create", "GET", "ROLES"));
            permissions.add(new Permission("UPDATE ROLE", "/admin/role/update", "POST", "ROLES"));
            permissions.add(new Permission("UPDATE ROLE", "/admin/role/update/{id}", "GET", "ROLES"));
            permissions.add(new Permission("DELETE ROLE", "/admin/role/{id}", "POST", "ROLES"));
            permissions.add(new Permission("FIND ROLE BY ID", "/admin/role/{id}", "GET", "ROLES"));

            permissions.add(new Permission("CATEGORY TABLE", "/staff/category", "GET", "CATEGORIES"));
            permissions.add(new Permission("CREATE CATEGORY PAGE", "/staff/category/create", "GET", "CATEGORIES"));
            permissions.add(new Permission("CREATE CATEGORY", "/staff/category/create", "GET", "CATEGORIES"));
            permissions.add(new Permission("UPDATE CATEGORY", "/staff/category/update", "POST", "CATEGORIES"));
            permissions.add(new Permission("UPDATE CATEGORY", "/staff/category/update/{id}", "GET", "CATEGORIES"));
            permissions.add(new Permission("DELETE CATEGORY", "/staff/category/{id}", "POST", "CATEGORIES"));
            permissions.add(new Permission("FIND ROLE BY ID", "/staff/category/{id}", "GET", "CATEGORIES"));
            this.permissionRepository.saveAll(permissions);
        }
        if (roleCnt == 0) {
            var roles = new ArrayList<Role>();

            var roleAdmin = new Role();
            roleAdmin.setPermissions(this.permissionRepository.findAll());
            roleAdmin.setName(DefineVariable.roleNameAdmin);
            roleAdmin.setDescription("Admin Golden Chicken");

            var roleCustomer = new Role();
            roleCustomer.setName(DefineVariable.roleNameCustomer);
            roleCustomer.setDescription("Customer Golden Chicken");

            var roleStaff = new Role();
            roleStaff.setName(DefineVariable.roleNameStaff);
            roleStaff.setDescription("Staff Golden Chicken");
            roles.add(roleAdmin);
            roles.add(roleCustomer);
            roles.add(roleStaff);

            this.roleRepository.saveAll(roles);
        }
        if (userCnt == 0) {
            var users = new ArrayList<User>();
            var admin = new User();
            admin.setFullName("SUPER_ADMIN");
            admin.setEmail(DefineVariable.email);
            admin.setStatus(true);
            admin.setPassword(this.passwordEncoder.encode(DefineVariable.password));
            admin.setRole(this.roleRepository.findByName(DefineVariable.roleNameAdmin));
            if (DefineVariable.roleNameAdmin.equals("ADMIN")) {
                var staffAdmin = new Staff();
                staffAdmin.setStaffType(StaffType.MANAGER);
                staffAdmin.setStatus(StaffStatus.AVAILABLE);
                staffAdmin.setUser(admin);
                admin.setStaff(staffAdmin);
            }

            var staff = new User();
            staff.setFullName("SUPER_STAFF");
            staff.setEmail("mylndce190083@gmail.com");
            staff.setStatus(true);
            staff.setPassword(this.passwordEncoder.encode(DefineVariable.password));
            staff.setRole(this.roleRepository.findByName(DefineVariable.roleNameStaff));
            if (DefineVariable.roleNameStaff.equals("STAFF")) {
                var superStaff = new Staff();
                superStaff.setStaffType(StaffType.RECEPTIONIST);
                superStaff.setStatus(StaffStatus.AVAILABLE);
                superStaff.setUser(staff);
                staff.setStaff(superStaff);
            }
            users.add(staff);
            users.add(admin);
            this.userRepository.saveAll(users);

        }
        if (roleCnt != 0 && userCnt != 0) {
            System.out.println(">>> SKIP PROCESSING INITIALIER <<<");
        } else {
            System.out.println(">>> INIT DATABASE SUCCESSFULL");
        }
    }

}
