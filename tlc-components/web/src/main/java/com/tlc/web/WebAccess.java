package com.tlc.web;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.commons.util.ConfLoader;
import com.tlc.web.undertow.UndertowService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Abishek
 * @version 1.0
 */
public final class WebAccess implements BundleActivator
{
    private static final AtomicReference<WebService> REFERENCE = new AtomicReference<>();

    private static void register(WebService service)
    {
        if(!REFERENCE.compareAndSet(null, service))
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service already initialized");
        }
    }

    private static WebService unregister()
    {
        return REFERENCE.getAndSet(null);
    }

    public static WebService get()
    {
        final WebService service = REFERENCE.get();
        if(service == null)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service not initialized");
        }
        else
        {
            return service;
        }
    }

    private BundleListener bundleListener;

    @Override
    public void start(BundleContext bundleContext) throws IOException
    {
        final UndertowService webService = new UndertowService();
        final Map<String, String> input = ConfLoader.load("web.undertow.cfg");
        webService.start(input);
        this.bundleListener = new BundleListener(webService);
        register(webService);
        bundleContext.addBundleListener(bundleListener);
    }

    @Override
    public void stop(BundleContext bundleContext)
    {
        final UndertowService webService = (UndertowService)unregister();
        webService.stop();
        bundleContext.removeBundleListener(bundleListener);
    }
}
