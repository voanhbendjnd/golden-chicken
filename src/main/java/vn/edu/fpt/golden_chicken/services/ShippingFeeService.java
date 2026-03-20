package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.ShippingFee;
import vn.edu.fpt.golden_chicken.domain.request.ShippingFeeDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResShippingFee;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.ShippingFeeRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ShippingFeeService {
    ShippingFeeRepository shippingFeeRepository;

    public BigDecimal getFeeByWard(String ward) {
        var sf = this.shippingFeeRepository.findByWardIgnoreCase(ward);
        if (sf != null) {
            return sf.getFee();
        }
        return null;
    }

    public void create(ShippingFeeDTO dto) {
        if (this.shippingFeeRepository.existsByWardIgnoreCase(dto.ward())) {
            throw new DataInvalidException("Xã/Phường này đã được tạo!");
        }
        var sf = new ShippingFee();
        sf.setFee(dto.fee());
        sf.setWard(dto.ward());
        this.shippingFeeRepository.save(sf);
    }

    public void update(ShippingFeeDTO dto) {
        if (this.shippingFeeRepository.existsByWardIgnoreCaseAndIdNot(dto.ward(), dto.id())) {
            throw new DataInvalidException("Trùng tên xã/phường!");
        }
        var sf = this.shippingFeeRepository.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException("ID phường xã (" + dto.id() + ") hong tồn tại!"));
        sf.setFee(dto.fee());
        sf.setWard(dto.ward());
        this.shippingFeeRepository.save(sf);
    }

    public ResShippingFee fetchById(Long id) {
        var sf = this.shippingFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ID của phường xã (" + id + ") không tìm thấy"));
        var res = new ResShippingFee();
        res.setId(sf.getId());
        res.setFee(sf.getFee());
        res.setWard(sf.getWard());
        res.setCreatedAt(sf.getCreatedAt());
        res.setUpdatedAt(sf.getUpdatedAt());
        return res;
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<ShippingFee> spec, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = this.shippingFeeRepository.findAll(spec, pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var sf = new ResShippingFee();
            sf.setId(x.getId());
            sf.setFee(x.getFee());
            sf.setCreatedAt(x.getCreatedAt());
            sf.setUpdatedAt(x.getUpdatedAt());
            sf.setWard(x.getWard());
            return sf;
        }).toList());
        return res;
    }

    public void delete(Long id) {
        var sf = this.shippingFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ID của phường xã (" + id + ") không tìm thấy"));
        this.shippingFeeRepository.delete(sf);
    }

}
