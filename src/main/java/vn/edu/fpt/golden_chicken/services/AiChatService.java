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

import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.ai.AiResponseDTO;
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
            List<Product> activeProducts = productRepository.fetchAllActiveAndCategoryActive();
            String dynamicMenu = activeProducts.stream()
                    .map(p -> String.format("[ID: %d] - %s (%.0f VND)", p.getId(), p.getName(), p.getPrice()))
                    .collect(Collectors.joining(", "));

            String systemPrompt = String.format(
                    "You are a witty, enthusiastic, and slightly sassy AI order assistant for the 'Golden Chicken' fast-food restaurant.\n\n"
                            +
                            "AVAILABLE MENU:\n%s\n\n" +
                            "CRITICAL RULES:\n" +
                            "1. Output Language: You MUST write the 'message' field in Vietnamese, matching your friendly and sassy persona.\n"
                            +
                            "2. Strict Menu: ONLY suggest or add items from the menu above. If a customer asks for something not on the menu (e.g., pizza, beef), playfully refuse and suggest your chicken items.\n"
                            +
                            "3. Smart Budgeting: If a customer gives a budget (e.g., 'Tôi có 100k'), intelligently calculate and suggest a combo within that price.\n"
                            +
                            "4. Output Format: You MUST return ONLY a single valid JSON object. DO NOT wrap it in ```json or use any markdown formatting.\n\n"
                            +
                            "JSON GENERATION CASES:\n" +
                            "- CASE 1 (Customer is just chatting, asking for advice, or has not confirmed an order):\n"
                            +
                            "  {\"action\": \"CHAT\", \"message\": \"[Your witty Vietnamese response here]\"}\n" +
                            "- CASE 2 (Customer explicitly wants to order or accepts your suggested combo):\n" +
                            "  {\"action\": \"ADD_TO_CART\", \"message\": \"[Your enthusiastic confirmation in Vietnamese]\", \"items\": [{\"productId\": 1, \"quantity\": 2}]}",
                    dynamicMenu);

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

            JsonNode root = objectMapper.readTree(responseStr);

            if (root.has("error")) {
                log.error("Gemini API returned an error: {}", root.path("error").path("message").asText());
                throw new Exception("Gemini API Error: " + root.path("error").path("message").asText());
            }

            if (!root.has("candidates") || root.path("candidates").isEmpty()) {
                log.error("Gemini response missing candidates: {}", responseStr);
                throw new Exception("Gemini response missing candidates");
            }

            String aiJsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
                    .asText();
            log.info("Extracted AI JSON text: {}", aiJsonText);

            if (aiJsonText.contains("```")) {
                aiJsonText = aiJsonText.replaceAll("```json|```", "").trim();
            }

            AiResponseDTO aiResponse = objectMapper.readValue(aiJsonText, AiResponseDTO.class);

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
            result.put("message",
                    "Sorry, I'm having a bit of trouble connecting to the kitchen. (" + e.getMessage() + ")");
            result.put("cartCount", 0);
            return result;
        }
    }
}
