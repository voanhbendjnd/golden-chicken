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
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public boolean tryConsume(String key) {
        Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
