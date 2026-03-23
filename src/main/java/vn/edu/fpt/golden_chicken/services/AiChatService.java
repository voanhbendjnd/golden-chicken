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

import vn.edu.fpt.golden_chicken.domain.response.ai.AiResponseDTO;
import vn.edu.fpt.golden_chicken.domain.response.ai.ProductSuggest;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;

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

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String modelName;

    public Map<String, Object> processChat(String chatMessage, Long customerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductSuggest> activeProducts = productRepository.getIdAndNameProductForAI();
            String dynamicMenu = activeProducts.stream()
                    .map(p -> String.format("%s (%.0f VND)", p.getName(), p.getPrice()))
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
                            "4. Output Format: You MUST return ONLY a single valid JSON object. DO NOT wrap it in ```json or use any markdown formatting.\n"
                            +
                            "5. If no product is found that meets the customer's needs, the response should be, Bạn vui lòng gửi lại 1 yêu cầu khác nha, hiện tại mình không thể tìm ra những sản phẩm phù hợp với yêu cầu của bạn đâu nè!\n"
                            +
                            "6. If the price exceeds the budget and the return request exceeds 33 items per product, add exactly 33 items to the cart and return fewer than 33 items, telling the customer that you only add a maximum of 33 items to their cart. If the customer asks for the worst product, say there are no bad products.\n"
                            +
                            "7. The only information you can include in the message for customers is the product name.\n"
                            +
                            "8. Absolutely do not return error codes in developer language; return natural language that everyone can understand.\n"
                            +
                            "JSON GENERATION CASES:\n" +
                            "- CASE 1 (Customer is just chatting, asking for advice, or has not confirmed an order):\n"
                            +
                            "  {\"action\": \"CHAT\", \"message\": \"[Your witty Vietnamese response here]\"}\n" +
                            "- CASE 2 (Customer explicitly wants to order or accepts your suggested combo):\n" +
                            "  {\"action\": \"ADD_TO_CART\", \"message\": \"[Your enthusiastic confirmation in Vietnamese]\", \"items\": [{\"productId\": 1, \"quantity\": 2}]}",
                    dynamicMenu);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", chatMessage);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", List.of(systemMessage, userMessage));
            payload.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String responseStr = restTemplate.postForObject(apiUrl, entity, String.class);
            log.info("Groq raw response: {}", responseStr);

            JsonNode root = objectMapper.readTree(responseStr);

            if (root.has("error")) {
                log.error("Groq API returned an error: {}", root.path("error").path("message").asText());
                throw new Exception("Groq API Error: " + root.path("error").path("message").asText());
            }

            if (!root.has("choices") || root.path("choices").isEmpty()) {
                log.error("Groq response missing choices: {}", responseStr);
                throw new Exception("Groq response missing choices");
            }

            String aiJsonText = root.path("choices").get(0).path("message").path("content").asText();
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
            result.put("message", "Ơ kìa 🤔 mình chưa hiểu ý bạn lắm, nói lại giúp mình nha!");
            result.put("cartCount", 0);
            return result;
        }
    }
}
