package com.tlc.validator.internal;

import com.tlc.validator.type.Email;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class EmailValidator implements ConstraintValidator<Email, String>
{
    @Override
    public boolean isValid(String emailId, ConstraintValidatorContext context)
    {
        if (emailId != null)
        {
            return emailId.length() <= 255 && ValidatorUtil.isValidEmail(emailId);
        }
        return true;
    }
}
