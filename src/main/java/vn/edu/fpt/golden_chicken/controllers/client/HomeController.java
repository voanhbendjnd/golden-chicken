package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String getHomePage() {
        return "client/home";
    }

    @GetMapping("/")
    public String getHomePage_home() {
        return "client/home";
    }

}

