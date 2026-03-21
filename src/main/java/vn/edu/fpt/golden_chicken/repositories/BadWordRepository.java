package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.BadWord;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long>, JpaSpecificationExecutor<BadWord> {
    boolean existsByWordIgnoreCase(String word);

    boolean existsByWordIgnoreCaseAndIdNot(String word, Long id);

    @Query("select b.word from BadWord b where b.status = true")
    List<String> fetchAllActiveWordsOnly();
}
