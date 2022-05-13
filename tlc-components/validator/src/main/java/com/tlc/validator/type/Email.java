package com.tlc.validator.type;

import com.tlc.validator.internal.EmailCollectionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Abishek
 * @version 1.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailCollectionValidator.class)
@Documented
public @interface Email
{
    String message() default "i18n_validator_error_invalid_email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
