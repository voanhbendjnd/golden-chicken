package vn.edu.fpt.golden_chicken.utils.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handle405(HttpRequestMethodNotSupportedException ex, Model model) {
        model.addAttribute("status", 405);
        model.addAttribute("error", "Method Not Allowed");
        model.addAttribute("message", ex.getMessage());
        return "error/405";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handEmailAlreadyExists(EmailAlreadyExistsException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "admin/user/table";
    }

    @ExceptionHandler(DataInvalidException.class)
    public String handleDataInvalidException(DataInvalidException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(value = { PermissionException.class })
    public String handlePermissionException(PermissionException ex) {
        return "client/auth/access-deny";
    }

    @ExceptionHandler(value = { CheckoutException.class })
    public String handleAmountSystemNotTheSame(CheckoutException ax) {
        return "error/checkout-error";
    }

    @ExceptionHandler(value = {
            AccountBanException.class
    })
    public String handleAccountBanException(AccountBanException ae) {
        return "error/account.ban";
    }

}