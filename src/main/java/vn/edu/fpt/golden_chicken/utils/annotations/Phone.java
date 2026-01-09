package vn.edu.fpt.golden_chicken.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.edu.fpt.golden_chicken.utils.annotations.handle.PhoneValidator;

@Constraint(validatedBy = PhoneValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {
    String message() default "Number phone invalid format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}