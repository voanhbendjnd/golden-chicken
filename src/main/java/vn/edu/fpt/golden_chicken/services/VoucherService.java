package vn.edu.fpt.golden_chicken.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.VoucherCreateDTO;
import vn.edu.fpt.golden_chicken.domain.request.VoucherUpdateDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResVoucher;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.repositories.VoucherRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherService {
    VoucherRepository repo;
    ProfileService profileService;
    CustomerRepository customerRepository;
    CustomerVoucherRepository customerVoucherRepository;

    //   @Transactional
    //    public Page<Artist> getAllArtist(int page,int size){
    //        Pageable pageable = PageRequest.of(page,size);
    //        return artistRepository.findAll(pageable);
    //    }
    @Transactional
    public Page<ResVoucher> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Voucher> voucherPage = repo.findByIsDeletedFalse(pageable);

        return voucherPage.map(v -> {
            ResVoucher res = new ResVoucher();
            res.setId(v.getId());
            res.setCode(v.getCode());
            res.setName(v.getName());
            res.setStatus(v.getStatus());
            return res;
        });
    }

    @Transactional
    public ResVoucher getById(Long id) {
        Voucher v = repo.findById(id).orElseThrow();
        ResVoucher res = new ResVoucher();
        res.setId(v.getId());
        res.setCode(v.getCode());
        res.setName(v.getName());
        res.setDescription(v.getDescription());
        res.setDiscountValue(v.getDiscountValue());
        res.setDiscountType(v.getDiscountType());
        res.setMinOrderValue(v.getMinOrderValue());
        res.setPointCost(v.getPointCost());
        res.setExchangeable(v.isExchangeable());
        res.setStatus(v.getStatus());
        res.setStartAt(v.getStartAt());
        res.setEndAt(v.getEndAt());
        return res;
    }

    @Transactional
    public void createVoucher(VoucherCreateDTO dto) {

        if (repo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists");
        }

        Voucher v = new Voucher();
        v.setCode(dto.getCode());
        v.setName(dto.getName());
        v.setDescription(dto.getDescription());
        v.setDiscountValue(dto.getDiscountValue());
        v.setDiscountType(dto.getDiscountType());
        v.setMinOrderValue(dto.getMinOrderValue());
        v.setPointCost(dto.getPointCost());
        v.setStartAt(dto.getStartAt());
        v.setEndAt(dto.getEndAt());
        v.setExchangeable(dto.getExchangeable());
        v.setStatus("DISABLED");

        if (v.getEndAt().isBefore(v.getStartAt())
                || v.getEndAt().isEqual(v.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if ("PERCENT".equals(dto.getDiscountType())
                && dto.getDiscountValue() > 100) {
            throw new IllegalArgumentException("Percent cannot exceed 100");
        }

        if (Boolean.TRUE.equals(dto.getExchangeable())
                && (dto.getPointCost() == null || dto.getPointCost() <= 0)) {
            throw new IllegalArgumentException("Point cost must be greater than 0");
        }

        repo.save(v);
    }

    @Transactional
    public void updateVoucher(VoucherUpdateDTO dto) {
        Voucher v = repo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setCode(dto.getCode());
        v.setName(dto.getName());
        v.setDescription(dto.getDescription());
        v.setDiscountValue(dto.getDiscountValue());
        v.setDiscountType(dto.getDiscountType());
        v.setMinOrderValue(dto.getMinOrderValue());
        v.setPointCost(dto.getPointCost());
        v.setStartAt(dto.getStartAt());
        v.setEndAt(dto.getEndAt());
        v.setStatus(dto.getStatus());
        v.setExchangeable(dto.getExchangeable());

        if (v.getEndAt().isBefore(v.getStartAt())
                || v.getEndAt().isEqual(v.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if ("PERCENT".equals(dto.getDiscountType())
                && dto.getDiscountValue() > 100) {
            throw new IllegalArgumentException("Percent cannot exceed 100");
        }
        // nếu exchangeable = true thì pointCost phải > 0
        if (Boolean.TRUE.equals(dto.getExchangeable())
                && (dto.getPointCost() == null || dto.getPointCost() <= 0))
            throw new IllegalArgumentException("Point cost must be greater than 0");
        repo.save(v);
    }

    @Transactional
    public void disableVoucher(Long id) {
        Voucher v = repo.findById(id).orElseThrow();
        v.setStatus("DISABLED");
        repo.save(v);
    }

    @Transactional
    public void deleteVoucher(Long id) {
        Voucher v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setIsDeleted(true);
        repo.save(v);
    }

    private ResVoucher toResVoucher(Voucher voucher) {
        ResVoucher res = new ResVoucher();
        res.setId(voucher.getId());
        res.setCode(voucher.getCode());
        res.setName(voucher.getName());
        res.setDescription(voucher.getDescription());
        res.setDiscountValue(voucher.getDiscountValue());
        res.setDiscountType(voucher.getDiscountType());
        res.setMinOrderValue(voucher.getMinOrderValue());
        res.setPointCost(voucher.getPointCost());
        res.setStartAt(voucher.getStartAt());
        res.setEndAt(voucher.getEndAt());
        return res;
    }

    public List<ResVoucher> getListVoucherForExchange() {
        refreshVoucherStatuses();
        List<Voucher> vouchers = repo.findAvailableForExchange();

        List<ResVoucher> resVouchers = new ArrayList<>();
        for (Voucher voucher : vouchers) {
            resVouchers.add(toResVoucher(voucher));
        }
        return resVouchers;

    }

    public long getPoints() throws PermissionException {
        long points = 0L;

        var currentUser = profileService.getCurrentUser();
        if (currentUser.getCustomer() == null) {
            throw new PermissionException("You do not have permission!");
        }
        if (currentUser != null) {
            var customer = customerRepository.findById(currentUser.getId()).orElse(null);
            points = customer.getPoint();
        }
        return points;
    }

    @Transactional
    public void redeemVoucher(Long voucherId) {
        var currentUser = profileService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Bạn cần đăng nhập.");
        }

        var customer = customerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng."));

        var voucher = repo.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại."));

        LocalDateTime now = LocalDateTime.now();

        if (Boolean.TRUE.equals(voucher.getIsDeleted())) {
            throw new IllegalArgumentException("Voucher đã bị xóa.");
        }
        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            throw new IllegalArgumentException("Voucher không còn hoạt động.");
        }
        if (!voucher.isExchangeable()) {
            throw new IllegalArgumentException("Voucher không hỗ trợ đổi điểm.");
        }
        if (voucher.getStartAt() == null || voucher.getEndAt() == null
                || now.isBefore(voucher.getStartAt())
                || now.isAfter(voucher.getEndAt())) {
            throw new IllegalArgumentException("Voucher đã hết hạn hoặc chưa tới thời gian áp dụng.");
        }
        if (voucher.getPointCost() == null || voucher.getPointCost() <= 0) {
            throw new IllegalArgumentException("Voucher chưa cấu hình điểm đổi hợp lệ.");
        }

        long currentPoints = customer.getPoint() == null ? 0L : customer.getPoint();
        if (currentPoints < voucher.getPointCost()) {
            throw new IllegalArgumentException("Không đủ điểm để đổi voucher.");
        }

        customer.setPoint(currentPoints - voucher.getPointCost());

        CustomerVoucher cv = new CustomerVoucher();
        cv.setCustomer(customer);
        cv.setVoucher(voucher);
        cv.setStatus(StatusVoucher.AVAILABLE);
        cv.setUsedAt(null);

        customerVoucherRepository.save(cv);
        customerRepository.save(customer);
    }

    public List<CustomerVoucher> getMyVouchers() {
        var currentUser = profileService.getCurrentUser();
        if (currentUser == null)
            return new ArrayList<>();

        // update voucher status
        refreshVoucherStatuses();
        var customer = customerRepository.findById(currentUser.getId()).orElse(null);
        if (customer == null)
            return new ArrayList<>();

        return customerVoucherRepository.findByCustomer(customer);
    }

    public List<CustomerVoucher> getRedeemHistory() {
        var currentUser = profileService.getCurrentUser();
        if (currentUser == null)
            return new ArrayList<>();
        refreshVoucherStatuses();
        var customer = customerRepository.findById(currentUser.getId()).orElse(null);
        if (customer == null)
            return new ArrayList<>();

        return customerVoucherRepository.findByCustomerOrderByRedeemedAtDesc(customer);
    }

    private void refreshVoucherStatuses() {
        refreshExpiredStatus();
        refreshCustomerVoucherExpiredStatus();
    }

    @Transactional
    public void refreshExpiredStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> activeVouchers = this.repo.findAllByStatus("ACTIVE");

        List<Voucher> toUpdate = new ArrayList<>();
        for (Voucher v : activeVouchers) {
            if (v.getEndAt() != null && now.isAfter(v.getEndAt())) {
                v.setStatus("EXPIRED");
                ;
                toUpdate.add(v);
            }
        }
        if (!toUpdate.isEmpty()) {
            repo.saveAll(toUpdate);
        }
    }

    @Transactional
    public void refreshCustomerVoucherExpiredStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<CustomerVoucher> availableList = customerVoucherRepository.findByStatus(StatusVoucher.AVAILABLE);

        List<CustomerVoucher> toUpdate = new ArrayList<>();

        for (CustomerVoucher cv : availableList) {
            Voucher voucher = cv.getVoucher();
            if (voucher == null)
                continue;

            boolean expiredByStatus = "EXPIRED".equalsIgnoreCase(voucher.getStatus());
            boolean expiredByTime = voucher.getEndAt() != null && now.isAfter(voucher.getEndAt());

            if (expiredByStatus || expiredByTime) {
                cv.setStatus(StatusVoucher.EXPIRED);
                toUpdate.add(cv);
            }
        }

        if (!toUpdate.isEmpty()) {
            customerVoucherRepository.saveAll(toUpdate);
        }
    }
}
