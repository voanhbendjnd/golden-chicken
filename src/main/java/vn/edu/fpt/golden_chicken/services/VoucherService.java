package vn.edu.fpt.golden_chicken.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.VoucherCreateDTO;
import vn.edu.fpt.golden_chicken.domain.request.VoucherUpdateDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResVoucher;
import vn.edu.fpt.golden_chicken.repositories.VoucherRepository;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherService {
    VoucherRepository repo;

    public List<ResVoucher> getAll() {
        return repo.findByIsDeletedFalse().stream().map(v -> {
            ResVoucher res = new ResVoucher();
            res.setId(v.getId());
            res.setCode(v.getCode());
            res.setName(v.getName());
            res.setStatus(v.getStatus());
            return res;
        }).toList();
    }
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
    public void createVoucher(VoucherCreateDTO dto) {
        // 1. check trùng code
        if (repo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists");
        }
        // 2. map DTO → Entity
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
        v.setExchangeable(dto.isExchangeable());
        v.setStatus("ACTIVE");
        repo.save(v);
    }
    public void updateVoucher(Long id, VoucherUpdateDTO dto) {
        Voucher v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setName(dto.getName());
        v.setDescription(dto.getDescription());
        v.setDiscountValue(dto.getDiscountValue());
        v.setDiscountType(dto.getDiscountType());
        v.setMinOrderValue(dto.getMinOrderValue());
        v.setPointCost(dto.getPointCost());
        v.setStartAt(dto.getStartAt());
        v.setEndAt(dto.getEndAt());
        v.setStatus(dto.getStatus());
        v.setExchangeable(dto.isExchangeable());
        repo.save(v);
    }
    public void disableVoucher(Long id) {
        Voucher v = repo.findById(id).orElseThrow();
        v.setStatus("DISABLED");
        repo.save(v);
    }
    public void deleteVoucher(Long id) {
        Voucher v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setIsDeleted(true);
        repo.save(v);
    }
}
