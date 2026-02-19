package vn.edu.fpt.golden_chicken.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class VNPayConfig {
    @Value("${vnp.payUrl}")
    private String payUrl;

    @Value("${vnp.tmnCode}")
    private String tmnCode;

    @Value("${vnp.hashSecret}")
    private String hashSecret;

    @Value("${vnp.apiUrl}")
    private String apiUrl;

    @Value("${vnp.returnUrl}")
    private String returnUrl;
}
