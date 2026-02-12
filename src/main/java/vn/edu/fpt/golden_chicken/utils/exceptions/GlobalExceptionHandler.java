package vn.edu.fpt.golden_chicken.utils.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        return "admin/user/table";
    }

    @ExceptionHandler(DataInvalidException.class)
    public String handleDataInvalidException(DataInvalidException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "admin/role/table";
    }

    @ExceptionHandler(value = { PermissionException.class })
    public String handlePermissionException(PermissionException ex) {
        return "client/auth/access-deny";
    }

    @ExceptionHandler(value = { CheckoutException.class })
    public String handleAmountSystemNotTheSame(CheckoutException ax) {
        return "error/checkout-error";
    }

}