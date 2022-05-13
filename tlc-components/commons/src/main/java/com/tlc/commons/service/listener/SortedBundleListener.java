package com.tlc.commons.service.listener;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Listener to manage the {@link Bundle} event change activities in prerequisite order of bundles
 *
 * @author Sunder
 * @version 1.0
 */
public abstract class SortedBundleListener implements org.osgi.framework.BundleListener {

    private final Set<String> STARTED_BUNDLES;
    private final Map<Bundle, Set<String>> WAITING_BUNDLES;
    private final Lock LOCK;
    private static final String BUNDLE_PREFIX = "com.tlc";
    private static final Logger LOGGER = LoggerFactory.getLogger(SortedBundleListener.class);

    protected SortedBundleListener() {
        this.LOCK = new ReentrantLock();
        this.STARTED_BUNDLES = new HashSet<>();
        this.WAITING_BUNDLES = new ConcurrentHashMap<>();
    }

    /**
     * Triggered when {@link Bundle} state changes
     *
     * @param event Bundle event
     */
    @Override
    public void bundleChanged(final BundleEvent event) {
        final Bundle bundle = event.getBundle();
        final String symbolicName = bundle.getSymbolicName();

        if (symbolicName.startsWith(BUNDLE_PREFIX)) {
            final int type = event.getType();

            LOGGER.info("Metafile listener action initiated for bundle : {}, Type : {}", symbolicName, type);

            if (type == BundleEvent.STARTED) {
                LOCK.lock();

                try {
                    if (!STARTED_BUNDLES.contains(symbolicName) && !WAITING_BUNDLES.containsKey(bundle)) {
                        processBundleStart(bundle, getPrerequisiteBundleSet(bundle));
                    } else {
                        LOGGER.info("Bundle started called before bundle stop : {}, Type : {}", symbolicName, type);
                    }
                } finally {
                    LOCK.unlock();
                }
            } else if (type == BundleEvent.STOPPED) {
                LOCK.lock();

                try {
                    bundleStopped(bundle);
                    STARTED_BUNDLES.remove(symbolicName);
                } finally {
                    LOCK.unlock();
                }
            } else if(type == BundleEvent.STOPPING) {
                LOCK.lock();

                try {
                    bundleStopping(bundle);
                } finally {
                    LOCK.unlock();
                }
            }
        }
    }

    /**
     * Retrieves non started prerequisite bundles from the bundle
     *
     * @param bundle Started bundle
     * @return Set of prerequisite bundles
     */
    private Set<String> getPrerequisiteBundleSet(final Bundle bundle) {
        final String prerequisiteBundlesTag = "Prerequisite-Bundles";
        final String prerequisiteBundles = bundle.getHeaders().get(prerequisiteBundlesTag);
        final Set<String> prerequisiteBundleSet = new LinkedHashSet<>();
        final String crmBundlePrefix = BUNDLE_PREFIX + ".crm";

        if (prerequisiteBundles != null) {
            final String[] prerequisiteBundlesArray = prerequisiteBundles.split(";");

            LOGGER.debug("Prerequisite bundles to be started before {} are {}", bundle.getSymbolicName(),
                    prerequisiteBundlesArray);

            for (final String prerequisiteBundleName : prerequisiteBundlesArray) {
                final String symbolicName = prerequisiteBundleName.trim();

                if (symbolicName.startsWith(crmBundlePrefix) && !STARTED_BUNDLES.contains(symbolicName)) {
                    if (WAITING_BUNDLES.keySet().stream().anyMatch(
                            waitingBundle -> symbolicName.equals(waitingBundle.getSymbolicName())
                                    && WAITING_BUNDLES.get(waitingBundle).contains(bundle.getSymbolicName()))) {
                        LOGGER.warn("Bundles {} and {} have a cyclic prerequisite for each other",
                                symbolicName, bundle.getSymbolicName());
                        LOGGER.warn("It may lead the startup activities of bundles in an unexpected order");
                    } else {
                        prerequisiteBundleSet.add(symbolicName);
                    }
                }
            }
        }
        return prerequisiteBundleSet;
    }

    /**
     * Processes the bundle startup activities
     *
     * @param bundle Started bundle
     * @param prerequisiteBundles Symbolic name set of prerequisite bundles
     */
    private void processBundleStart(final Bundle bundle, final Set<String> prerequisiteBundles) {
        final String symbolicName = bundle.getSymbolicName();

        if (prerequisiteBundles.isEmpty()) {
            bundleStarted(bundle);
            STARTED_BUNDLES.add(symbolicName);

            WAITING_BUNDLES.remove(bundle);
            removeFromPrerequisites(symbolicName);

            LOGGER.info("Bundle startup activities done {}", symbolicName);
            startWaitingBundles();
        } else {
            LOGGER.info("Bundle waiting for prerequisite bundles startup activities to be done {}", symbolicName);
            WAITING_BUNDLES.putIfAbsent(bundle, prerequisiteBundles);
        }
    }

    /**
     * Starts the waiting bundles whose prerequisites are resolved
     */
    private void startWaitingBundles() {
        for (final Map.Entry<Bundle, Set<String>> waitingEntry : WAITING_BUNDLES.entrySet()) {
            final Bundle waitingBundle = waitingEntry.getKey();
            final Set<String> prerequisiteBundleSet = waitingEntry.getValue();

            if (!STARTED_BUNDLES.contains(waitingBundle.getSymbolicName()) && prerequisiteBundleSet.isEmpty()) {
                processBundleStart(waitingBundle, prerequisiteBundleSet);
            }
        }
    }

    /**
     * Performs the {@link Bundle} startup activities
     *
     * @param bundle Started bundle
     */
    public void bundleStarted(final Bundle bundle) {
    }

    /**
     * Removes the given symbolic name from the set of prerequisites of waiting bundles
     *
     * @param symbolicName Symbolic name of prerequisite bundle
     */
    private void removeFromPrerequisites(final String symbolicName) {
        for (final Set<String> prerequisites : WAITING_BUNDLES.values()) {
            prerequisites.remove(symbolicName);
        }
    }

    /**
     * Performs the activities should be done after the {@link Bundle} stopped
     *
     * @param bundle Stopped bundle
     */
    public void bundleStopped(final Bundle bundle) {
    }

    /**
     * Performs the activities should be done while the {@link Bundle} is stopping
     *
     * @param bundle Stopping bundle
     */
    public void bundleStopping(final Bundle bundle) {
    }
}
