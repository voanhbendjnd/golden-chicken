package vn.edu.fpt.golden_chicken.services;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.BadWord;
import vn.edu.fpt.golden_chicken.domain.request.BadWordDTO;
import vn.edu.fpt.golden_chicken.domain.response.BadWordResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.BadWordRepository;
import vn.edu.fpt.golden_chicken.utils.BadWordFilterUtility;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BadWordService {
    BadWordRepository badWordRepository;
    KafkaTemplate<String, String> kafkaScanOldReview;
    BadWordFilterUtility badWordFilterUtility;

    public void create(BadWordDTO dto) {
        if (this.badWordRepository.existsByWordIgnoreCase(dto.getWord())) {
            throw new DataInvalidException("Từ cấm là (" + dto.getWord() + ") đã tồn tại!");
        }
        var badWord = new BadWord();
        badWord.setWord(dto.getWord());
        badWord.setStatus(dto.getStatus());
        var currentBadWord = this.badWordRepository.save(badWord);
        this.badWordFilterUtility.loadAndCompileBadWords();
        if (Boolean.TRUE.equals(dto.getApplyFromNowOn())) {
            if (currentBadWord.getStatus()) {
                kafkaScanOldReview.send("scan-old-reviews-topic", dto.getWord());
            }
        } else {
            reloadAllReview();
        }
    }

    public void update(BadWordDTO dto) {
        if (this.badWordRepository.existsByWordIgnoreCaseAndIdNot(dto.getWord(), dto.getId())) {
            throw new DataInvalidException("Từ cấm là (" + dto.getWord() + ") đã tồn tại");
        }
        var badWord = this.badWordRepository.findById(dto.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Từ cấm với ID là (" + dto.getId() + " ) không tìm thấy!"));
        badWord.setStatus(dto.getStatus());
        badWord.setWord(dto.getWord());
        var currentBadWord = this.badWordRepository.save(badWord);
        this.badWordFilterUtility.loadAndCompileBadWords();
        if (Boolean.TRUE.equals(dto.getApplyFromNowOn())) {
            if (currentBadWord.getStatus()) {
                kafkaScanOldReview.send("scan-old-reviews-topic", dto.getWord());
            }
        } else {
            reloadAllReview();
        }

    }

    public void delete(Long id, Boolean applyFromNowOn) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Từ cấm với ID là (" + id + " ) không tìm thấy!"));
        this.badWordRepository.delete(badWord);
        this.badWordFilterUtility.loadAndCompileBadWords();
        if (Boolean.FALSE.equals(applyFromNowOn)) {
            reloadAllReview();
        }
    }

    public void revertStatus(Long id, Boolean applyFromNowOn) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Từ cấm với ID (" + id + " ) không tìm thấy!"));
        badWord.setStatus(!badWord.getStatus());
        var current = this.badWordRepository.save(badWord);
        this.badWordFilterUtility.loadAndCompileBadWords();
        if (Boolean.TRUE.equals(applyFromNowOn)) {
            if (current.getStatus()) {
                kafkaScanOldReview.send("scan-old-reviews-topic", badWord.getWord());

            }
        } else {
            reloadAllReview();
        }
    }

    public BadWordResponse fetchById(Long id) {
        var badWord = this.badWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Từ cấm với ID (" + id + ") không tìm thấy!"));
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

    public void reloadAllReview() {
        this.badWordFilterUtility.loadAndCompileBadWords();
    }
}
