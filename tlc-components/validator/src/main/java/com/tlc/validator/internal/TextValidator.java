package com.tlc.validator.internal;

import com.tlc.validator.type.Text;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class TextValidator implements ConstraintValidator<Text, String>
{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context)
    {
        return value == null || value.length() <= 2500;
    }
}