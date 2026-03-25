package vn.edu.fpt.golden_chicken.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    // Lưu trữ các bucket cho từng mã định danh (Email hoặc IP)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Kiểm tra và thực hiện trừ token từ bucket.
     * Cấu hình mặc định: tối đa 1 request mỗi 60 giây (giống các sàn TMĐT).
     *
     * @param key Định danh (Ví dụ: Email hoặc IP)
     * @return true nếu còn token, false nếu vượt quá giới hạn
     */
    public boolean tryConsume(String key) {
        Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String key) {
        // Giới hạn 1 request mỗi 1 phút
        Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
