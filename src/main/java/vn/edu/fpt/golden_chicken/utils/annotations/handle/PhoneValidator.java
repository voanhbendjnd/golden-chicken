package vn.edu.fpt.golden_chicken.utils.annotations.handle;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.edu.fpt.golden_chicken.utils.annotations.Phone;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null)
            return false;
        // Kiểm tra logic: ví dụ phải là số và có độ dài từ 10-11 ký tự
        return phone.matches("\\d{10,11}");
    }
}