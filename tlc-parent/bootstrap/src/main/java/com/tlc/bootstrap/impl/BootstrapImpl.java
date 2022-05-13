package com.tlc.bootstrap.impl;

import com.tlc.bootstrap.Bootstrap;
import com.tlc.bootstrap.FeatureManager;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "system.bootstrap")
public class BootstrapImpl implements Bootstrap
{
    private FeatureManagerImpl featureManager;
    private Version version;

    @Reference
    private FeaturesService featuresService;

    @Reference
    private Scheduler scheduler;

    private AtomicBoolean appStatus;

    private final static Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    @Activate
    public void activate(BundleContext context, Map<String, String> input) throws Exception
    {
        LOGGER.info("Initializing Instance");

        final String startupFeatures = Objects.requireNonNull(input.get("startup.feature"), "No valid features configured!!");
        final String dynamicRepo = input.get("dynamic.repositories");

        printEnvironmentInfo();

        this.version = context.getBundle().getVersion();
        this.appStatus = new AtomicBoolean(false);
        if(dynamicRepo == null)
        {
            this.featureManager = new FeatureManagerImpl(featuresService);
        }
        else
        {
            final String[] repositories = dynamicRepo.split(",");
            final Set<URI> uris = new HashSet<>();
            for (String repository : repositories)
            {
                uris.add(new URI(repository.trim()));
            }
            this.featureManager = new FeatureManagerImpl(featuresService, uris);
        }
        final Set<String> featureSet = Arrays.stream(startupFeatures.split(",")).map(String::trim).collect(Collectors.toSet());
        scheduler.schedule((Runnable) () ->
        {
            try
            {
                featureManager.installFeatures(featureSet);
                appStatus.set(true);
                LOGGER.info("Startup features successfully initialized");
            }
            catch (Exception exp)
            {
                LOGGER.error("Startup features failed to initialize", exp);
            }
        }, scheduler.NOW());
        LOGGER.info("Successfully Initialized Instance");
    }

    @Modified
    public void modified(Map<String, String> input) throws Exception
    {
        final String dynamicRepo = input.get("dynamic.repositories");
        LOGGER.info("Bootstrap config modified, dynamicRepo : {}", dynamicRepo);
        final String[] repositories = dynamicRepo.split(",");
        final Set<URI> uris = new HashSet<>();
        for (String repository : repositories)
        {
            uris.add(URI.create(repository.trim()));
        }
        featureManager.updateDynamicRepositories(uris);
    }

    @Deactivate
    public void deactivate()
    {
        this.version = null;
        this.featureManager = null;
        this.appStatus = null;
    }

    @Override
    public String getVersion()
    {
        return version.toString();
    }

    @Override
    public boolean isAppInitialized()
    {
        return appStatus.get();
    }

    @Override
    public FeatureManager getFeatureManager()
    {
        return featureManager;
    }

    private void printEnvironmentInfo()
    {
        final String userName = System.getProperty("user.name");
        final long maxMemory = Runtime.getRuntime().maxMemory();

        final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        final String arch = operatingSystemMXBean.getArch();
        final String name = operatingSystemMXBean.getName();
        final int processors = operatingSystemMXBean.getAvailableProcessors();
        final String version = operatingSystemMXBean.getVersion();

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final String vmVendor = runtimeMXBean.getVmVendor();
        final String vmName = runtimeMXBean.getVmName();
        final String vmVersion = runtimeMXBean.getVmVersion();
        final String home = System.getProperty("java.home");

        LOGGER.info("System - Processors : {}, Memory : {}GB", processors, String.format("%.2f", (maxMemory / (1000.0 * 1000.0))));
        LOGGER.info("OperatingSystem - Name : {}, Arch : {}, Version : {}, User : {}", name, arch, version, userName);
        LOGGER.info("Session - Name : {}, Arch : {}, Version : {}, Processors : {}, User : {}", name, arch, version, processors, userName);
        LOGGER.info("Jvm - Vendor : {}, VM : {}, Version : {}, home : {} ", vmVendor, vmName, vmVersion, home);
    }
}
