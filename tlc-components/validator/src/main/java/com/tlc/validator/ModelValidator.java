package com.tlc.validator;


import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * @author Abishek
 * @version 1.0
 */
public interface ModelValidator
{
    <T extends TlcModel> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups);

    <T extends TlcModel> boolean isValid(T object, Class<?>... groups);
}
