package vn.edu.fpt.golden_chicken.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Category;
import vn.edu.fpt.golden_chicken.domain.request.CategoryDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResCategory;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.CategoryRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryService {
    CategoryRepository categoryRepository;

    public void create(CategoryDTO dto) {
        var name = dto.getName();
        if (this.categoryRepository.existsByName(name)) {
            throw new DataInvalidException("Category With Name (" + name + ") Already Exists!");
        }
        var category = new Category();
        category.setName(name);
        category.setStatus(dto.getStatus());
        category.setDescription(dto.getDescription());
        this.categoryRepository.save(category);
    }

    public void update(CategoryDTO dto) {
        var name = dto.getName();
        var id = dto.getId();
        if (this.categoryRepository.existsByNameAndIdNot(name, id)) {
            throw new DataInvalidException("Category With Name (" + name + ") Already Exists!");
        }
        var category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new DataInvalidException("Category With ID (" + id + ") Already Exists!"));
        category.setName(name);
        category.setStatus(dto.getStatus());
        category.setDescription(dto.getDescription());
        this.categoryRepository.save(category);
    }

    public ResCategory findById(long id) {
        var category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", id));
        var res = new ResCategory();
        res.setDescription(category.getDescription());
        res.setId(id);
        res.setName(category.getName());
        res.setStatus(category.getStatus());
        return res;
    }

    public void updateStatus(Long id, String condition) {
        var category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new DataInvalidException("Category With ID (" + id + ") Already Exists!"));
        if (condition.equalsIgnoreCase("on")) {
            category.setStatus(true);
        } else {
            category.setStatus(false);
        }
        this.categoryRepository.save(category);
    }

    public ResultPaginationDTO fetchWithPagination(Specification<Category> spec, Pageable pageable) {
        var page = this.categoryRepository.findAll(spec, pageable);
        var res = new ResultPaginationDTO();
        var mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        res.setMeta(mt);
        res.setResult(page.getContent().stream().map(x -> {
            var resCate = new ResCategory();
            resCate.setId(x.getId());
            resCate.setName(x.getName());
            resCate.setDescription(x.getDescription());
            resCate.setStatus(x.getStatus());
            return resCate;
        }).collect(Collectors.toList()));
        return res;
    }

    public List<ResCategory> fectchAll() {
        return this.categoryRepository.findAll().stream().map(x -> {
            var res = new ResCategory();
            res.setDescription(x.getDescription());
            res.setId(x.getId());
            res.setStatus(x.getStatus());
            res.setName(x.getName());
            return res;
        }).collect(Collectors.toList());
    }

}
