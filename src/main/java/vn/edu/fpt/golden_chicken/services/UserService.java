package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResUser;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.StaffRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StaffStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;
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
    PasswordEncoder passwordEncoder;

    @Transactional
    public void create(UserDTO request) {
        var role = this.roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role ID",
                        request.getRoleId() != null ? request.getRoleId() : DefineVariable.roleNameCustomer));
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

    public void register(UserDTO request) {
        var email = request.getEmail();
        if (this.userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        var roleCustomer = this.roleRepository.findByName(DefineVariable.roleNameCustomer);

        if (roleCustomer != null) {
            var user = new User();
            user.setEmail(request.getEmail().toLowerCase());
            user.setFullName(request.getFullName());
            user.setStatus(true);
            user.setPhone(user.getPhone());
            user.setRole(roleCustomer);
            user.setPassword(this.passwordEncoder.encode(request.getPassword()));
            var customer = new Customer();
            customer.setUser(user);
            user.setCustomer(customer);
            this.userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("ROLE", DefineVariable.roleNameCustomer);
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

    public ResUser update(UserDTO request) {
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

    public ResUser findById(long id) {
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

    public void importUsers(MultipartFile file) throws IOException, EmailAlreadyExistsException {
        var is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);

        var sheet = workbook.getSheetAt(0);
        var users = new ArrayList<User>();

        for (var row : sheet) {
            if (row.getRowNum() == 0)
                continue;
            var user = new User();
            var email = row.getCell(0).getStringCellValue();
            if (this.userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException(email);
            }
            user.setEmail(email);
            user.setPassword(this.passwordEncoder.encode(row.getCell(1).getStringCellValue()));
            user.setFullName(row.getCell(2).getStringCellValue());

            String roleType = row.getCell(3).getStringCellValue();

            if ("STAFF".equalsIgnoreCase(roleType)) {
                var staff = new Staff();

                try {
                    String typeStr = row.getCell(4).getStringCellValue();
                    staff.setStaffType(StaffType.valueOf(typeStr.toUpperCase()));

                    String statusStr = row.getCell(5).getStringCellValue();
                    staff.setStatus(StaffStatus.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    staff.setStaffType(StaffType.SHIPPER);
                }

                staff.setUser(user);
                user.setRole(this.roleRepository.findByName("STAFF"));
                user.setStaff(staff);
            } else {
                user.setRole(this.roleRepository.findByName("CUSTOMER"));
                var customer = new Customer();
                customer.setUser(user);
                user.setCustomer(customer);
            }

            user.setStatus("TRUE".equalsIgnoreCase(row.getCell(6).getStringCellValue()) ? true : false);
            // var active = row.getCell(6);
            // if (active != null) {
            // if (active.getCellType() == CellType.BOOLEAN) {
            // user.setStatus(active.getBooleanCellValue());
            // } else if (active.getCellType() == CellType.STRING) {
            // user.setStatus("TRUE".equalsIgnoreCase(active.getStringCellValue()));
            // }
            // } else {
            // user.setStatus(false);
            // }

            users.add(user);
        }
        this.userRepository.saveAll(users);
    }

}
