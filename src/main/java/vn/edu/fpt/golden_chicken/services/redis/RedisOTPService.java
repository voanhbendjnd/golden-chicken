package vn.edu.fpt.golden_chicken.services.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisOTPService {
    StringRedisTemplate stringRedisTemplate;

    public void saveOTP(String email, String OTP) {
        this.stringRedisTemplate.opsForValue().set(DeclareConstant.USER_OTP + email, OTP, 3, TimeUnit.MINUTES);
    }
}
