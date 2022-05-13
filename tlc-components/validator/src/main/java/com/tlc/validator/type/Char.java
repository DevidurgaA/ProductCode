package com.tlc.validator.type;

import com.tlc.validator.internal.CharValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Abishek
 * @version 1.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CharValidator.class)
@Documented
public @interface Char
{
    String message() default "i18n_validator_error_invalid_char";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
