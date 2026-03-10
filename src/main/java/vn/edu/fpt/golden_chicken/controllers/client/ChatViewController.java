package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping("/chat")
    public String getChatPage(Model model) {
        // Giả sử Ben đã có logic lấy User hiện tại từ Spring Security context
        // Để test nhanh, mình cứ trả về trang "chat"
        return "client/chat";
    }
}