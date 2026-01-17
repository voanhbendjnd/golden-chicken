package vn.edu.fpt.golden_chicken.utils.annotations.handle;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.utils.annotations.StrongPassword;

public class HandleStrongPassword implements ConstraintValidator<StrongPassword, UserDTO> {

    @Override
    public boolean isValid(UserDTO request, ConstraintValidatorContext context) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return false;
        }
        return true;
    }

}
