package vn.edu.fpt.golden_chicken.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class BadWordFilterUtility {

    private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
            "thối", "tanh", "hôi", "ôi thiu", "ươn", "mốc", "khó ăn", "dở tệ",
            "ghê", "ghê tởm", "kinh dị", "bẩn", "dơ", "bẩn thỉu", "khó nuốt",
            "hôi hám", "tanh tưởi", "mùi thối", "mùi hôi", "mùi tanh"));

    public boolean isViolating(String comment) {

        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        // 1️⃣ lowercase
        String normalized = comment.toLowerCase();

        // 2️⃣ remove dấu tiếng Việt
        normalized = removeAccent(normalized);

        // 3️⃣ remove ký tự đặc biệt nhưng giữ space
        normalized = normalized.replaceAll("[^a-z\\s]", " ");

        // 4️⃣ gộp nhiều space
        normalized = normalized.replaceAll("\\s+", " ").trim();

        // 5️⃣ check theo word boundary
        for (String badWord : BLACKLIST) {

            String normalizedBadWord = removeAccent(badWord);

            Pattern pattern = Pattern.compile("\\b" + normalizedBadWord + "\\b");

            if (pattern.matcher(normalized).find()) {
                System.out.println("Phát hiện từ cấm: " + badWord);
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
