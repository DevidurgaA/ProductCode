package com.tlc.validator.type;

import com.tlc.validator.internal.KCharValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Abishek
 * @version 1.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = KCharValidator.class)
@Documented
public @interface KChar
{
    String message() default "i18n_validator_error_invalid_char";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
