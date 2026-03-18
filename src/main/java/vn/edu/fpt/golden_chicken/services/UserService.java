package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.common.MyLibrary;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.RegisterDTO;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResUser;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.UserMessage;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.StaffRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StaffStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;
import vn.edu.fpt.golden_chicken.utils.converts.UserConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("null")

public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    StaffRepository staffRepository;
    CustomerRepository customerRepository;
    PasswordEncoder passwordEncoder;
    OrderRepository orderRepository;
    CartRepository cartRepository;
    KafkaTemplate<String, UserMessage> kafkaUserMessage;
    KafkaTemplate<String, String> kafkaRecoverPwMessage;

    // KafkaTemplate<String, VerifyAccountMessage> msgVerifyAccount;
    public void forceLogoutCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                request.getSession().invalidate();
            }

            SecurityContextHolder.clearContext();

            System.out.println("Đã ép người dùng hiện tại đăng xuất thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi khi force logout: " + e.getMessage());
        }
    }

    public User getUserInContext() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            return null;
        }
        return this.userRepository.findByEmailIgnoreCaseAndStatus(email, true);
    }

    // @Transactional
    public void create(UserDTO request) {
        var role = this.roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role ID",
                        request.getRoleId() != null ? request.getRoleId() : DeclareConstant.roleNameCustomer));
        var user = UserConvert.toUser(request);
        user.setRole(role);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new DataInvalidException("Password and confirm password not the same!");
        }
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
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
            this.customerRepository.save(customer);
        }
        var userMsg = new UserMessage();
        userMsg.setReason("Staff create new account");
        userMsg.setEmail(request.getEmail());
        userMsg.setName(request.getFullName());
        userMsg.setPassword(request.getPassword());
        userMsg.setSendAt(LocalDateTime.now());
        this.kafkaUserMessage.send("create-account-topic", userMsg);
        // this.mailService.configBeforeSendForStaff(request.getFullName(),
        // request.getEmail(), request.getPassword());

    }

    public void register(RegisterDTO request) {
        var email = request.getEmail();
        if (this.userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        var roleCustomer = this.roleRepository.findByName(DeclareConstant.roleNameCustomer);

        if (roleCustomer != null) {
            var user = new User();
            user.setEmail(request.getEmail().toLowerCase());
            user.setFullName(request.getName());
            user.setStatus(true);
            // user.setPhone(user.getPhone());
            user.setRole(roleCustomer);
            user.setPassword(request.getPassword());
            var customer = new Customer();
            customer.setUser(user);
            user.setCustomer(customer);
            this.userRepository.save(user);
            // this.mailService.startOTP(email, this.generateOTP(user), email);
            // this.mailService.allowMailForUser(request.getFullName(), email);
        } else {
            throw new ResourceNotFoundException("ROLE", DeclareConstant.roleNameCustomer);
        }

    }

    public String getMaskedCustomerName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Khách ẩn danh";
        }

        fullName = fullName.trim();
        int length = fullName.length();
        if (length <= 2) {
            return fullName.charAt(0) + "***";
        }
        return fullName.charAt(0) + "***" + fullName.substring(length - 1);
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
        user.setStatus(request.getStatus());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        return UserConvert.toUserRes(this.userRepository.save(user));
    }

    public ResUser findById(long id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
        return UserConvert.toUserRes(user);
    }

    public boolean checkLockedAccount(String email) {
        var user = this.userRepository.findByEmailIgnoreCaseAndStatus(email, true);
        if (user.getStaff() != null) {
            return true;
        }
        var customer = user.getCustomer();
        if (customer.getLockedUntil() != null && customer.getLockedUntil().isAfter(LocalDateTime.now())) {
            return false;
        }
        if (customer.getLockedUntil() != null && customer.getLockedUntil().isBefore(LocalDateTime.now())) {
            customer.setLockedUntil(null);
            customer.setViolationCount(0);
            this.customerRepository.save(customer);
            return true;
        }
        return true;

    }

    public void revertStatusAccount(Long id) throws PermissionException {
        var admin = this.getUserInContext();
        if (admin.getStaff() != null) {
            var user = this.userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
            var status = !user.getStatus();
            if (!admin.getId().equals(user.getId())) {
                user.setStatus(status);
                this.userRepository.save(user);
            } else {
                throw new PermissionException("Không thể thay đổi trạng thái của chính tài khoản đang sử dụng!");
            }

        }

    }

    public void deleteById(long id) throws PermissionException {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id));
        var userLogin = this.getUserInContext();
        if (user.getEmail().equalsIgnoreCase(userLogin.getEmail())) {
            throw new PermissionException("Không thể tự xóa chính tài khoản!");
        }
        if (user.getStaff() != null) {
            var staff = user.getStaff();
            if (this.orderRepository.existsByShipperId(staff.getId())) {
                user.setStatus(false);
                this.userRepository.save(user);
                return;
            }
        } else if (user.getCustomer() != null) {
            var customer = user.getCustomer();
            if (this.orderRepository.existsByCustomerId(customer.getId())
                    || this.cartRepository.existsByCustomerId(customer.getId())) {
                user.setStatus(false);
                this.userRepository.save(user);
                return;
            }
        }

        this.userRepository.delete(user);

    }

    public User getByEmail(String email) {
        return this.userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(rollbackFor = Exception.class)
    public void importUsers(MultipartFile file) throws IOException, DataFormatException {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Invalid file format. Please upload file excel(.xlsx)");
        }

        try (var is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            var sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new DataFormatException("File Excel Not Empty!");
            }
            var users = new ArrayList<User>();
            var mpAcc = new HashMap<ResUser, String>();
            var existingEmails = this.userRepository.findAll().stream()
                    .map(User::getEmail)
                    .collect(Collectors.toSet());

            var pattern = Pattern.compile(MyLibrary.EMAIL_PATTERN);

            for (var row : sheet) {
                int rowNum = row.getRowNum();

                if (rowNum == 0) {
                    if (row.getCell(0) == null || !"Email".equalsIgnoreCase(row.getCell(0).getStringCellValue())) {
                        throw new DataFormatException("Invalid Header: Column 1 must be 'Email'");
                    }
                    continue;
                }

                if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isBlank()) {
                    continue;
                }

                if (row.getCell(1) == null || row.getCell(2) == null
                        || row.getCell(3) == null || row.getCell(6) == null) {
                    throw new DataFormatException("Data invalid at row " + (rowNum + 1) + ": Missing required cells.");
                }

                var email = row.getCell(0).getStringCellValue().trim();

                if (!pattern.matcher(email).matches()) {
                    throw new DataFormatException(
                            "Error at row " + (rowNum + 1) + ": Invalid Email format (" + email + ")");
                }

                if (existingEmails.contains(email)) {
                    throw new EmailAlreadyExistsException(
                            "Error at row " + (rowNum + 1) + ": Email " + email);
                }

                var user = new User();
                user.setEmail(email);
                var password = row.getCell(1).getStringCellValue();
                var name = row.getCell(2).getStringCellValue();
                user.setPassword(this.passwordEncoder.encode(password));
                user.setFullName(name);
                var resUser = new ResUser();
                resUser.setEmail(email);
                resUser.setFullName(name);
                mpAcc.put(resUser, password);
                String roleType = row.getCell(3).getStringCellValue().toUpperCase();

                if ("STAFF".equals(roleType)) {
                    var staff = new Staff();
                    try {
                        String typeStr = row.getCell(4).getStringCellValue();
                        staff.setStaffType(StaffType.valueOf(typeStr.toUpperCase()));

                        String statusStr = row.getCell(5).getStringCellValue();
                        staff.setStatus(StaffStatus.valueOf(statusStr.toUpperCase()));
                    } catch (Exception e) {
                        throw new DataFormatException(
                                "Error at row " + (rowNum + 1) + ": Invalid Staff Type or Staff Status.");
                    }
                    staff.setUser(user);
                    user.setRole(this.roleRepository.findByName("STAFF"));
                    user.setStaff(staff);
                } else if ("CUSTOMER".equals(roleType)) {
                    user.setRole(this.roleRepository.findByName("CUSTOMER"));
                    var customer = new Customer();
                    customer.setUser(user);
                    user.setCustomer(customer);
                } else {
                    throw new DataFormatException(
                            "Error at row " + (rowNum + 1) + ": Unknown Role Type '" + roleType + "'");
                }

                user.setStatus("TRUE".equalsIgnoreCase(row.getCell(6).getStringCellValue()));
                users.add(user);
            }

            if (!users.isEmpty()) {
                this.userRepository.saveAll(users);
                mpAcc.forEach((user, password) -> {
                    CompletableFuture.runAsync(() -> {
                        var userMsg = new UserMessage();
                        userMsg.setEmail(user.getEmail());
                        userMsg.setName(user.getFullName());
                        userMsg.setReason("Staff create new account");
                        userMsg.setPassword(password);
                        this.kafkaUserMessage.send("create-account-topic", userMsg);
                        // this.mailService.configBeforeSendForStaff(user.getFullName(),
                        // user.getEmail(), password);
                    });
                });
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public int countCustomer() {
        return this.userRepository.countCustomer();
    }

    public String generateBase() {
        var ran = new Random();
        var code = ran.nextInt(1000, 9999) + "";
        return code;
    }

    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return this.userRepository.existsByEmailIgnoreCase(email.trim().toLowerCase());
    }

    public void updateCustomerPoint(Customer customer, Long point, boolean action) throws PermissionException {
        var currentPoint = customer.getPoint() != null ? customer.getPoint() : 0;
        if (action) {
            customer.setPoint(currentPoint + point);
        } else {
            var lastPoint = currentPoint - point;
            if (lastPoint <= 0) {
                lastPoint = 0;
            }
            customer.setPoint(lastPoint);
        }
        this.customerRepository.save(customer);

    }

    public void allowUpdatePassword(String email) {
        var user = this.userRepository.findByEmailIgnoreCaseAndStatus(email, true);
        user.setUpdatePassword(true);
        this.userRepository.save(user);
    }

    public void changePassword(String email, String password) {
        var user = this.userRepository.findByEmailIgnoreCaseAndStatus(email, true);
        user.setPassword(this.passwordEncoder.encode(password));
        user.setUpdatePassword(false);
        this.userRepository.save(user);
        this.kafkaRecoverPwMessage.send("state-account-topic", user.getEmail());
    }

    public boolean getAllowChangePassword(String email) {
        var user = this.userRepository.findByEmailIgnoreCaseAndStatus(email, true);
        return user.getUpdatePassword() ? true : false;
    }

}
