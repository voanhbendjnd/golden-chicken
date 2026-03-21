package vn.edu.fpt.golden_chicken.utils;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import vn.edu.fpt.golden_chicken.repositories.BadWordRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BadWordFilterUtility {
    private final BadWordRepository badWordRepository;
    private final ReviewRepository reviewRepository;
    private List<Pattern> badWordPatterns = new ArrayList<>();

    public BadWordFilterUtility(BadWordRepository badWordRepository, ReviewRepository reviewRepository) {
        this.badWordRepository = badWordRepository;
        this.reviewRepository = reviewRepository;
    }

    public boolean isViolating(String comment) {
        if (comment == null || comment.trim().isEmpty())
            return false;
        String normalized = removeAccent(comment.toLowerCase())
                .replaceAll("[^a-z\\s]", " ").replaceAll("\\s+", " ").trim();
        for (Pattern pattern : badWordPatterns) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    public boolean isViolatingV1(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }
        String normalized = comment.toLowerCase();
        normalized = removeAccent(normalized);
        normalized = normalized.replaceAll("[^a-z\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        var setBadWord = this.badWordRepository.fetchAllActiveWordsOnly().stream().collect(Collectors.toSet());
        for (String badWord : setBadWord) {
            if (badWord == null || badWord.isBlank()) {
                continue;
            }
            String normalizedBadWord = removeAccent(badWord).toLowerCase()
                    .replaceAll("[^a-z\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (normalizedBadWord.isEmpty()) {
                continue;
            }
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(normalizedBadWord) + "\\b",
                    Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }

        return false;
    }

    @PostConstruct
    public void loadAndCompileBadWords() {
        badWordPatterns.clear();
        List<String> badWords = badWordRepository.fetchAllActiveWordsOnly();
        for (String word : badWords) {
            if (word != null && !word.isBlank()) {
                String normalizedWord = removeAccent(word.toLowerCase())
                        .replaceAll("[^a-z\\s]", " ").replaceAll("\\s+", " ").trim();

                if (!normalizedWord.isEmpty()) {
                    badWordPatterns.add(
                            Pattern.compile("\\b" + Pattern.quote(normalizedWord) + "\\b", Pattern.CASE_INSENSITIVE));
                }
            }
        }
    }

    public boolean containsBadWordThoroughly(String comment, String badWord) {
        if (comment == null || comment.trim().isEmpty() || badWord == null || badWord.isBlank()) {
            return false;
        }
        String normalizedComment = removeAccent(comment.toLowerCase())
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ").trim();
        String normalizedBadWord = removeAccent(badWord.toLowerCase())
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ").trim();
        if (normalizedBadWord.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(normalizedBadWord) + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(normalizedComment).find();
    }

    private String removeAccent(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public boolean isViolatingWithSet(String badWord) {
        var setReview = this.reviewRepository.getAllComment().stream().collect(Collectors.toSet());
        if (setReview.contains(badWord.toLowerCase())) {
            return true;
        }
        return false;

    }
}
