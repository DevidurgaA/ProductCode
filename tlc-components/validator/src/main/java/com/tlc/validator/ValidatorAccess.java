package com.tlc.validator;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.validator.internal.ModelValidatorImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Abishek
 * @version 1.0
 */
public final class ValidatorAccess implements BundleActivator
{
    private static final AtomicReference<ModelValidator> REFERENCE = new AtomicReference<>();
    private static void register(ModelValidator validator)
    {
        if(!REFERENCE.compareAndSet(null, validator))
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service already initialized");
        }
    }

    private static ModelValidator unregister()
    {
        return REFERENCE.getAndSet(null);
    }

    public static ModelValidator get()
    {
        final ModelValidator validator = REFERENCE.get();
        if(validator == null)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service not initialized");
        }
        else
        {
            return validator;
        }
    }

    @Override
    public void start(BundleContext bundleContext)
    {
        final ModelValidatorImpl modelValidator = new ModelValidatorImpl();
        modelValidator.start(new HashMap<>());
        register(modelValidator);
    }

    @Override
    public void stop(BundleContext bundleContext)
    {
        final ModelValidatorImpl modelValidator = (ModelValidatorImpl)unregister();
        modelValidator.stop();
    }
}
