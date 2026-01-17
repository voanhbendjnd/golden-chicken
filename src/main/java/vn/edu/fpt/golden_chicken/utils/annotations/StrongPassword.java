package vn.edu.fpt.golden_chicken.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.edu.fpt.golden_chicken.utils.annotations.handle.HandleStrongPassword;

@Constraint(validatedBy = HandleStrongPassword.class)
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Password And Confirm Password Not Matching!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
