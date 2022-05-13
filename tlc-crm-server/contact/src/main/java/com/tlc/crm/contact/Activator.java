package com.tlc.crm.contact;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Abishek
 * @version 1.0
 */
public class Activator implements BundleActivator
{
    @Override
    public void start(BundleContext bundleContext)
    {
        System.out.println("test");
    }

    @Override
    public void stop(BundleContext bundleContext)
    {

    }
}
