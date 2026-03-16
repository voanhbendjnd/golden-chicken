package vn.edu.fpt.golden_chicken.services;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.BadWord;
import vn.edu.fpt.golden_chicken.domain.request.BadWordDTO;
import vn.edu.fpt.golden_chicken.domain.response.BadWordResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.BadWordRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BadWordService {
    BadWordRepository badWordRepository;

    public void create(BadWordDTO dto) {
        if (this.badWordRepository.existsByWordIgnoreCase(dto.word())) {
            throw new DataInvalidException("Words with (" + dto.word() + ") already exists!");
        }
        var badWord = new BadWord();
        badWord.setWord(dto.word());
        badWord.setStatus(dto.status());
        this.badWordRepository.save(badWord);
    }

    public void update(BadWordDTO dto) {
        if (this.badWordRepository.existsByWordIgnoreCaseAndIdNot(dto.word(), dto.id())) {
            throw new DataInvalidException("Words with (" + dto.word() + ") already exists!");
        }
        var badWord = this.badWordRepository.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException("Bad word with id (" + dto.id() + " ) not found!"));
        badWord.setStatus(dto.status());
        badWord.setWord(dto.word());
        this.badWordRepository.save(badWord);
    }

    public void delete(Long id) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bad word with ID(" + id + " ) not found!"));
        this.badWordRepository.delete(badWord);
    }

    public void revertStatus(Long id) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bad word with ID(" + id + " ) not found!"));
        badWord.setStatus(!badWord.getStatus());
        this.badWordRepository.save(badWord);
    }

    public BadWordResponse fetchById(Long id) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bad word with ID (" + id + ") not found!"));
        var res = new BadWordResponse();
        res.setId(badWord.getId());
        res.setStatus(badWord.getStatus());
        res.setWord(badWord.getWord());
        return res;
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<BadWord> spec, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        var page = this.badWordRepository.findAll(spec, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var result = new BadWordResponse();
            result.setId(x.getId());
            result.setStatus(x.getStatus());
            result.setWord(x.getWord());
            return result;
        }).toList());
        return res;
    }

}
