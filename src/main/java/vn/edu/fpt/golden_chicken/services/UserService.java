package vn.edu.fpt.golden_chicken.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserRequest;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.UserRes;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.StaffRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StaffStatus;
import vn.edu.fpt.golden_chicken.utils.converts.UserConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private final static String city_constant = "Thành Phố Cần Thơ";
    UserRepository userRepository;
    RoleRepository roleRepository;
    StaffRepository staffRepository;
    CustomerRepository customerRepository;

    @Transactional
    public void create(UserRequest request) {
        var role = this.roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role ID", request.getRoleId()));
        var user = UserConvert.toUser(request);
        user.setRole(role);
        this.userRepository.save(user);
        if (role.getName().equals("STAFF")) {
            var staff = new Staff();
            staff.setUser(user);
            staff.setStatus(StaffStatus.AVAILABLE);
            staff.setStaffType(request.getStaffType());
            this.staffRepository.save(staff);
        } else if (role.getName().equals("CUSTOMER")) {
            var customer = new Customer();
            customer.setUser(user);
            if (request.getAddress() != null && !request.getAddress().isEmpty()) {
                customer.setAddress(request.getAddress() + ", " + request.getWard() + ", " + request.getDistrict()
                        + ", " + city_constant);
            }

            this.customerRepository.save(customer);
        }

    }

    public ResultPaginationDTO fetchAllWithPagination(Pageable pageable, Specification<User> spec) {
        var page = this.userRepository.findAll(spec, pageable);
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(UserConvert::toUserRes).collect(Collectors.toList()));
        return res;
    }

    public UserRes update(UserRequest request) {
        var id = request.getId();
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        var role = this.roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("User ID", request.getRoleId()));
        if (role.getName().equalsIgnoreCase("STAFF")) {
            user.getStaff().setStaffType(request.getStaffType());
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        return UserConvert.toUserRes(this.userRepository.save(user));
    }

    public UserRes findById(long id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
        return UserConvert.toUserRes(user);
    }

    public void deleteById(long id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
        user.setStatus(false);
        this.userRepository.save(user);

    }

    public User getByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

}
