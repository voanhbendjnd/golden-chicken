package vn.edu.fpt.golden_chicken.services.redis;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.request.RegisterDTO;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessage;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisUserService {
    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;
    UserService userService;

    private String getChatKey(String id1, String id2) {
        String first = id1 != null ? id1 : "";
        String second = id2 != null ? id2 : "";
        if (first.compareTo(second) > 0) {
            String temp = first;
            first = second;
            second = temp;
        }
        return "CHAT:HISTORY:" + first + ":" + second;
    }

    public Set<String> getChatPartners(String user) {
        return this.stringRedisTemplate.opsForSet().members("CHAT:PARTNERS:" + user);
    }

    public List<ChatMessage> getChatHistory(String user1, String user2) {
        var key = this.getChatKey(user1, user2);

        List<String> rawMessages = this.stringRedisTemplate.opsForList().range(key, 0, -1);

        if (rawMessages == null || rawMessages.isEmpty()) {
            return Collections.emptyList();
        }

        return rawMessages.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatMessage.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void saveNumberOfLoginFailures(String email) {
        String key = "LOGIN_FAIL:" + email;
        Long currentFailures = this.stringRedisTemplate.opsForValue().increment(key);
        if (currentFailures != null && currentFailures == 1) {
            this.stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
    }

    public Integer getNumberOfLoginFailures(String email) {
        var key = "LOGIN_FAIL:" + email;
        var value = this.stringRedisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    public void resetLoginFailures(String email) {
        this.stringRedisTemplate.delete("LOGIN_FAIL:" + email);
    }

    public void savePendingUserRegister(RegisterDTO dto) {
        try {
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

    public void lockAccount(String email) {
        var key = "LOCK:" + email;
        this.stringRedisTemplate.opsForValue().set(key, "LOCKED", 1, TimeUnit.MINUTES);
        this.stringRedisTemplate.delete("LOGIN_FAIL:" + email);
    }

    public boolean isAccountLocked(String email) {
        String key = "LOCK:" + email;
        return Boolean.TRUE.equals(this.stringRedisTemplate.hasKey(key));
    }

    public void saveChatMessageToRedis(ChatMessage message) {
        try {

            String key = this.getChatKey(message.getSenderId(), message.getRecipientId());
            String jsonMessage = objectMapper.writeValueAsString(message);
            this.stringRedisTemplate.opsForList().rightPush(key, jsonMessage);
            this.stringRedisTemplate.opsForList().trim(key, 0, 49);
            this.stringRedisTemplate.expire(key, 2, TimeUnit.HOURS);

            String customerId = "STAFF".equals(message.getRecipientId()) ? message.getSenderId()
                    : message.getRecipientId();
            if (!"STAFF".equals(customerId)) {
                this.stringRedisTemplate.opsForSet().add("CHAT:PARTNERS:STAFF", customerId);
                this.stringRedisTemplate.expire("CHAT:PARTNERS:STAFF", 2, TimeUnit.HOURS);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Lỗi parse tin nhắn: " + e.getMessage());
        }
    }
}
