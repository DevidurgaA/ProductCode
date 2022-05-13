package com.tlc.i18n;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.i18n.internal.I18nServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Abishek
 * @version 1.0
 */
public final class I18nAccess implements BundleActivator
{
    private static final AtomicReference<I18nService> REFERENCE = new AtomicReference<>();

    private static void register(I18nService service)
    {
        if(!REFERENCE.compareAndSet(null, service))
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service already initialized");
        }
    }

    private I18nService unregister()
    {
        return REFERENCE.getAndSet(null);
    }

    public static I18nService get()
    {
        final I18nService service = REFERENCE.get();
        if(service == null)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service not initialized");
        }
        else
        {
            return service;
        }
    }

    private I18nResResolver i18nResResolver;

    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        final I18nServiceImpl i18nService = new I18nServiceImpl();
        i18nService.start(new HashMap<>());
        this.i18nResResolver = new I18nResResolver(i18nService);
        bundleContext.addBundleListener(i18nResResolver);
        register(i18nService);
    }

    @Override
    public void stop(BundleContext bundleContext)
    {
        bundleContext.removeBundleListener(i18nResResolver);
        final I18nServiceImpl i18nService = (I18nServiceImpl)unregister();
        i18nService.stop();
    }
}
