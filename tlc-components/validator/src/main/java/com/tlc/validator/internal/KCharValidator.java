package com.tlc.validator.internal;

import com.tlc.validator.type.KChar;
import com.tlc.validator.Util;
import com.tlc.i18n.I18nKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class KCharValidator implements ConstraintValidator<KChar, I18nKey>
{
    @Override
    public boolean isValid(I18nKey keys, ConstraintValidatorContext context)
    {
        return keys == null || (keys.getKey().length() <= 512 && Util.isValid_withSpace(keys.getKey()));
    }
}