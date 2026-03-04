package vn.edu.fpt.golden_chicken.services.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisOTPService {
    StringRedisTemplate stringRedisTemplate;

    public void saveOTP(String email, String OTP) {
        this.stringRedisTemplate.opsForValue().set(email, OTP, 5, TimeUnit.MINUTES);
    }
}
