package vn.edu.fpt.golden_chicken.config;

import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
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

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DABASE <<<");
        long roleCnt = this.roleRepository.count();
        long userCnt = this.userRepository.count();
        if (roleCnt == 0) {
            var roleAdmin = new Role();
            var roleCustomer = new Role();
            var roles = new ArrayList<Role>();
            roleAdmin.setName(DefineVariable.roleNameAdmin);
            roleAdmin.setDescription("Admin Golden Chicken");
            roleCustomer.setName(DefineVariable.roleNameCustomer);
            roleCustomer.setDescription("Customer Golden Chicken");
            roles.add(roleAdmin);
            roles.add(roleCustomer);
            this.roleRepository.saveAll(roles);
        }
        if (userCnt == 0) {
            var admin = new User();
            admin.setFullName("ADMIN");
            admin.setEmail(DefineVariable.email);
            admin.setStatus(true);
            admin.setPassword(this.passwordEncoder.encode(DefineVariable.password));
            admin.setRole(this.roleRepository.findByName(DefineVariable.roleNameAdmin));
            if (DefineVariable.roleNameAdmin.equals("ADMIN")) {
                var staff = new Staff();
                staff.setStaffType(StaffType.MANAGER);
                staff.setStatus(StaffStatus.AVAILABLE);
                staff.setUser(admin);
                admin.setStaff(staff);
            }
            this.userRepository.save(admin);

        }
        if (roleCnt != 0 && userCnt != 0) {
            System.out.println(">>> SKIP PROCESSING INITIALIER <<<");
        } else {
            System.out.println(">>> INIT DATABASE SUCCESSFULL");
        }
    }

}
