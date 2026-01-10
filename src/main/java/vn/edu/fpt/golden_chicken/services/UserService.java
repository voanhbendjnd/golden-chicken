package vn.edu.fpt.golden_chicken.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserRequest;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.UserRes;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.converts.UserConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;

    public void create(UserRequest request) {
        this.userRepository.save(UserConvert.toUser(request));

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

    public User update(UserRequest request) {
        var id = request.getId();
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id + ""));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(request.getPhone());
        return this.userRepository.save(user);
    }

    public UserRes findById(long id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id + ""));
        return UserConvert.toUserRes(user);
    }

    public void deleteById(long id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID", id + ""));
        user.setStatus(false);

    }

}
