package com.tlc.validator.internal;

import com.tlc.validator.type.Char;
import com.tlc.validator.Util;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class CharValidator implements ConstraintValidator<Char, String>
{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context)
    {
        return value == null || (value.length() <= 255 && Util.isValid_withSpace(value));
    }
}