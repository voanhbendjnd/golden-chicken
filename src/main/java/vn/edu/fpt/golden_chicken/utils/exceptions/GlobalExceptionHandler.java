package vn.edu.fpt.golden_chicken.utils.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404"; // Trỏ đến file WEB-INF/views/error/404.jsp
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handEmailAlreadyExists(EmailAlreadyExistsException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(DataInvalidException.class)
    public String handleDataInvalidException(DataInvalidException ex, RedirectAttributes ra,
            HttpServletRequest request) {
        ra.addAttribute("errorMessage", ex.getMessage());
        var referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/role");
    }
}