package vn.edu.fpt.golden_chicken.services.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.request.RegisterDTO;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisUserService {
    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;
    UserService userService;

    public void savePendingUserRegister(RegisterDTO dto) {
        try {
            // change object to json type
            var jsonUser = this.objectMapper.writeValueAsString(dto);
            this.stringRedisTemplate.opsForValue().set(DeclareConstant.PENDING_USER + dto.getEmail(), jsonUser, 10,
                    TimeUnit.MINUTES);
        } catch (JsonProcessingException je) {
            throw new DataInvalidException("Dữ liệu truyền về không hợp lệ!");
        }

    }

    public boolean verifyAndCreateUser(String email, String otp) {
        var jsonUser = this.stringRedisTemplate.opsForValue().get(DeclareConstant.PENDING_USER + email);
        if (jsonUser == null) {
            throw new DataInvalidException("Yêu cầu đã hết hạn!");
        }
        try {
            var in4 = this.objectMapper.readValue(jsonUser, RegisterDTO.class);
            var otpRedis = this.stringRedisTemplate.opsForValue().get(DeclareConstant.USER_OTP + email);
            if (otpRedis != null && otpRedis.equals(otp)) {
                this.userService.register(in4);
                this.stringRedisTemplate.delete(DeclareConstant.PENDING_USER + email);
                this.stringRedisTemplate.delete(DeclareConstant.USER_OTP + email);
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
