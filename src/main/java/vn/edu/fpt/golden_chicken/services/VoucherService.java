package vn.edu.fpt.golden_chicken.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.VoucherCreateDTO;
import vn.edu.fpt.golden_chicken.domain.request.VoucherUpdateDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResVoucher;
import vn.edu.fpt.golden_chicken.repositories.VoucherRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherService {
    VoucherRepository repo;

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

}
