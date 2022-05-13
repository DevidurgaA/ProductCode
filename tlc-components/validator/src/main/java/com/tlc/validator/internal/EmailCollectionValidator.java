package com.tlc.validator.internal;

import com.tlc.validator.type.EmailCollection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * @author Abishek
 * @version 1.0
 */
public class EmailCollectionValidator implements ConstraintValidator<EmailCollection, Collection<String>>
{
    @Override
    public boolean isValid(Collection<String> emailIds, ConstraintValidatorContext context)
    {
        if (emailIds != null)
        {
            for (String emailId : emailIds)
            {
                if (emailId.length() > 255 || !ValidatorUtil.isValidEmail(emailId))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
