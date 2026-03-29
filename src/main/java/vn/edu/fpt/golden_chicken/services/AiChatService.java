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
import vn.edu.fpt.golden_chicken.domain.response.ai.ItemDTO;
import vn.edu.fpt.golden_chicken.domain.response.ai.ProductSuggest;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;

import java.util.ArrayList;
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

    public Map<String, Object> processChat(String chatMessage, Long customerId, List<Map<String, String>> history) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductSuggest> activeProducts = productRepository.getIdAndNameProductForAI();
            String dynamicMenu = activeProducts.stream()
                    .map(p -> String.format("[%d] %s (%.0f VND)", p.getId(), p.getName(), p.getPrice()))
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
                            "5. If no product is found that meets the customer's needs, the response should be, 'Bạn vui lòng gửi lại 1 yêu cầu khác nha, hiện tại mình không thể tìm ra những sản phẩm phù hợp với yêu cầu của bạn đâu nè!'\n"
                            +
                            "6. Max 33 items per product. If requested more, add exactly 33 and tell the customer you reached the limit.\n"
                            +
                            "7. The 'message' can ONLY mention product names from the menu.\n"
                            +
                            "8. Avoid developer jargon; use natural, witty language.\n"
                            +
                            "9. When you want to suggest products but NOT add them immediately, use action 'SUGGEST' and say 'Mình muốn gợi ý...' in the message.\n"
                            +
                            "10. You are only allowed to offer a total product price that is lower than or equal to the customer's price; do not suggest a higher total price."
                            +
                            "JSON GENERATION CASES:\n" +
                            "- CASE 1 (Chatting/Advice/Query): {\"action\": \"CHAT\", \"message\": \"[Vietnamese response]\"}\n"
                            +
                            "- CASE 2 (Direct order confirm): {\"action\": \"ADD_TO_CART\", \"message\": \"[Vietnamese confirmation]\", \"items\": [{\"productId\": 1, \"quantity\": 2}]}\n"
                            +
                            "- CASE 3 (Suggesting items): {\"action\": \"SUGGEST\", \"message\": \"[Vietnamese suggestion with 'Mình muốn gợi ý...']\", \"items\": [{\"productId\": 1, \"quantity\": 1}]}",
                    dynamicMenu);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(systemMessage);

            if (history != null) {
                for (Map<String, String> h : history) {
                    Map<String, Object> hm = new HashMap<>();
                    hm.put("role", h.get("role"));
                    hm.put("content", h.get("content"));
                    messages.add(hm);
                }
            }

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", chatMessage);
            messages.add(userMessage);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", messages);
            payload.put("response_format", Map.of("type", "json_object"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null) {
                headers.setBearerAuth(apiKey);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            if (apiUrl == null) {
                throw new Exception("apiUrl is not configured");
            }
            String responseStr = restTemplate.postForObject(apiUrl, entity, String.class);
            log.info("Groq raw response: {}", responseStr);

            if (responseStr == null) {
                throw new Exception("Groq API returned null response");
            }
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
                    activeProducts.stream()
                            .filter(p -> p.getId().equals(item.getProductId()))
                            .findFirst()
                            .ifPresent(p -> {
                                item.setName(p.getName());
                                item.setPrice(p.getPrice().doubleValue());
                                item.setImg(p.getImg());
                            });
                }
            } else if ("SUGGEST".equalsIgnoreCase(aiResponse.getAction()) && aiResponse.getItems() != null) {
                for (var item : aiResponse.getItems()) {
                    activeProducts.stream()
                            .filter(p -> p.getId().equals(item.getProductId()))
                            .findFirst()
                            .ifPresent(p -> {
                                item.setName(p.getName());
                                item.setPrice(p.getPrice().doubleValue());
                                item.setImg(p.getImg());
                            });
                }
            }

            result.put("message", aiResponse.getMessage());
            result.put("action", aiResponse.getAction());
            result.put("items", aiResponse.getItems());
            result.put("cartCount", cartService.sumCart());
            return result;

        } catch (Exception e) {
            log.error("AI Chat Error Details: {}", e.getMessage(), e);
            result.put("message",
                    "Ơ kìa 🤔 mình chưa hiểu ý bạn lắm, nhưng bạn thử xem qua mấy món này của nhà Golden Chicken xem sao nha!");
            result.put("action", "SUGGEST");

            List<ProductSuggest> fallbacks = productRepository.getIdAndNameProductForAI().stream().limit(3)
                    .collect(Collectors.toList());
            List<ItemDTO> fallbackItems = fallbacks.stream().map(p -> ItemDTO.builder()
                    .productId(p.getId())
                    .name(p.getName())
                    .price(p.getPrice().doubleValue())
                    .img(p.getImg())
                    .quantity(1)
                    .build()).collect(Collectors.toList());

            result.put("items", fallbackItems);
            result.put("cartCount", cartService.sumCart());
            return result;
        }
    }
}
