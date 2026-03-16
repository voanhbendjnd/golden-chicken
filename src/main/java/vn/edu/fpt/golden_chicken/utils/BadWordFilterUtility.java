package vn.edu.fpt.golden_chicken.utils;

import org.springframework.stereotype.Component;

import vn.edu.fpt.golden_chicken.repositories.BadWordRepository;

import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BadWordFilterUtility {
    private final BadWordRepository badWordRepository;

    public BadWordFilterUtility(BadWordRepository badWordRepository) {
        this.badWordRepository = badWordRepository;
    }

    // private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
    // "thối", "tanh", "hôi", "ôi thiu", "ươn", "mốc", "khó ăn", "dở tệ",
    // "ghê", "ghê tởm", "kinh dị", "bẩn", "dơ", "bẩn thỉu", "khó nuốt",
    // "hôi hám", "tanh tưởi", "mùi thối", "mùi hôi", "mùi tanh"));

    public boolean isViolating(String comment) {

        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        String normalized = comment.toLowerCase();

        normalized = removeAccent(normalized);

        normalized = normalized.replaceAll("[^a-z\\s]", " ");

        normalized = normalized.replaceAll("\\s+", " ").trim();
        var setBadWord = this.badWordRepository.fetchAllActiveWordsOnly().stream().collect(Collectors.toSet());
        for (String badWord : setBadWord) {

            String normalizedBadWord = removeAccent(badWord);

            Pattern pattern = Pattern.compile("\\b" + normalizedBadWord + "\\b");

            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }

        return false;
    }

    private String removeAccent(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
