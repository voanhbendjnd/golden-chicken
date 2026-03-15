package vn.edu.fpt.golden_chicken.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.fpt.golden_chicken.domain.dto.ai.AiResponseDTO;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final RestTemplate restTemplate;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public Map<String, Object> processChat(String chatMessage, Long customerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. Fetch active products and build menu string
            List<Product> activeProducts = productRepository.findByActiveTrue();
            String dynamicMenu = activeProducts.stream()
                    .map(p -> String.format("[ID: %d] - %s (%.0f VND)", p.getId(), p.getName(), p.getPrice()))
                    .collect(Collectors.joining(", "));

            // 2. Construct the System Prompt
            String systemPrompt = String.format(
                    "You are a witty AI assistant for Golden Chicken.\n" +
                    "Menu: %s\n" +
                    "You MUST return ONLY a valid JSON. DO NOT use markdown like ```json.\n" +
                    "If customer just chats, return: {\"action\": \"CHAT\", \"message\": \"...\"}\n" +
                    "If customer orders, return: {\"action\": \"ADD_TO_CART\", \"message\": \"...\", \"items\": [{\"productId\": 1, \"quantity\": 2}]}",
                    dynamicMenu
            );

            // 3. Prepare Gemini API Payload
            String fullPrompt = systemPrompt + "\n\nCustomer message: " + chatMessage;

            Map<String, Object> part = new HashMap<>();
            part.put("text", fullPrompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", Collections.singletonList(content));

            // 4. Call Gemini API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String responseStr = restTemplate.postForObject(apiUrl + apiKey, entity, String.class);
            log.info("Gemini raw response: {}", responseStr);
            
            // 5. Extract JSON string from Gemini response
            JsonNode root = objectMapper.readTree(responseStr);
            
            // Check for potential errors in response
            if (root.has("error")) {
                log.error("Gemini API returned an error: {}", root.path("error").path("message").asText());
                throw new Exception("Gemini API Error: " + root.path("error").path("message").asText());
            }

            if (!root.has("candidates") || root.path("candidates").isEmpty()) {
                log.error("Gemini response missing candidates: {}", responseStr);
                throw new Exception("Gemini response missing candidates");
            }

            String aiJsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            log.info("Extracted AI JSON text: {}", aiJsonText);

            // Robustness: Strip markdown code blocks if AI ignores instructions
            if (aiJsonText.contains("```")) {
                aiJsonText = aiJsonText.replaceAll("```json|```", "").trim();
            }

            // 6. Parse AI JSON into AiResponseDTO
            AiResponseDTO aiResponse = objectMapper.readValue(aiJsonText, AiResponseDTO.class);

            // 7. Handle ADD_TO_CART action
            if ("ADD_TO_CART".equalsIgnoreCase(aiResponse.getAction()) && aiResponse.getItems() != null) {
                for (var item : aiResponse.getItems()) {
                    cartService.addToCart(customerId, item.getProductId(), item.getQuantity());
                }
            }

            result.put("message", aiResponse.getMessage());
            result.put("cartCount", cartService.sumCart());
            return result;

        } catch (Exception e) {
            log.error("AI Chat Error Details: {}", e.getMessage(), e);
            result.put("message", "Sorry, I'm having a bit of trouble connecting to the kitchen. (" + e.getMessage() + ")");
            result.put("cartCount", 0);
            return result;
        }
    }
}
