package com.tlc.validator.internal;

import com.tlc.commons.service.Service;
import com.tlc.validator.ModelValidator;
import com.tlc.validator.TlcModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author Abishek
 * @version 1.0
 */

public class ModelValidatorImpl implements ModelValidator, Service
{
    private Validator validator;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelValidatorImpl.class);

    @Override
    public void start(Map<String, String> input)
    {
        final ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory();
        this.validator = validatorFactory.getValidator();
        LOGGER.info("Validator Service Activated");
    }

    @Override
    public void stop()
    {
        this.validator = null;
        LOGGER.info("Validator Service Deactivated");
    }

    @Override
    public final <T extends TlcModel> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups)
    {
        return validator.validate(object, groups);
    }

    @Override
    public final <T extends TlcModel> boolean isValid(T object, Class<?>... groups)
    {
        final Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
        return constraintViolations.size() == 0;
    }
}
