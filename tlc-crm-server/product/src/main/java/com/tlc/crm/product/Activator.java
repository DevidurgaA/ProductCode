package com.tlc.crm.product;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Selvakumar
 * @version 1.0
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Product module started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Product module stopped");
    }
}
