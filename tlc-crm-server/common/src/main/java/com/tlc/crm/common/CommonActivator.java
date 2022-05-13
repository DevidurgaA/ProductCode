package com.tlc.crm.common;

import com.tlc.crm.common.action.secure.list.StructureHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class CommonActivator implements BundleActivator
{
    @Override
    public void start(BundleContext bundleContext)
    {

    }

    @Override
    public void stop(BundleContext bundleContext)
    {
        StructureHandler.get().clearCache();
    }
}
