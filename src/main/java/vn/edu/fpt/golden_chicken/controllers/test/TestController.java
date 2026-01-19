package vn.edu.fpt.golden_chicken.controllers.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    // Mapping này tồn tại
    @GetMapping("/test-exists")
    public String testExists() {
        return "test/page";
    }

    // KHÔNG có mapping cho "/test-not-exists"
    // → Spring Boot sẽ tự throw NoResourceFoundException
}
