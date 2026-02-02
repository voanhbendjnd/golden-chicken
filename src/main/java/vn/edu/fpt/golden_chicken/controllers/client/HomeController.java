package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({ "/", "/home" })
    public String getHomePage(Model model) {
        model.addAttribute("activePage", "home");
        return "client/home";
    }

    @GetMapping("/ve-jollibee")
    public String getAboutUsPage(Model model) {
        model.addAttribute("activePage", "about");
        return "client/about";
    }
}
