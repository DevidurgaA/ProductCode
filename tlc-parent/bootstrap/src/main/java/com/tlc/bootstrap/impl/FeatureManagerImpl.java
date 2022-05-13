package com.tlc.bootstrap.impl;

import com.tlc.bootstrap.FeatureManager;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


class FeatureManagerImpl implements FeatureManager
{
    private final FeaturesService featuresService;

    private final Lock lock;

    private final Set<URI> dynamicRepositories;

    private static final EnumSet<FeaturesService.Option> OPTIONS = EnumSet.noneOf(FeaturesService.Option.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    FeatureManagerImpl(FeaturesService featuresService) throws Exception
    {
        this(featuresService, Set.of());
    }

    FeatureManagerImpl(FeaturesService featuresService, Collection<URI> dynamicRepos) throws Exception
    {
        this.featuresService = Objects.requireNonNull(featuresService);
        this.lock = new ReentrantLock();
        this.dynamicRepositories = new HashSet<>();
        updateDynamicRepositories(dynamicRepos);
    }

    void updateDynamicRepositories(Collection<URI> dynamicRepos) throws Exception
    {
        lock.lock();
        try
        {
            dynamicRepositories.clear();
            for (URI uri : dynamicRepos)
            {
                if(!repositoryExists(uri))
                {
                    addRepository(uri);
                }
                dynamicRepositories.add(uri);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean repositoryExists(URI uri) throws Exception
    {
        lock.lock();
        try
        {
            return featuresService.getRepository(uri) != null;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void addRepository(URI uri) throws Exception
    {
        lock.lock();
        try
        {
            LOGGER.info("Configuring repository, uri : {}", uri);
            final Repository repository = featuresService.getRepository(uri);
            if(repository == null)
            {
                featuresService.addRepository(uri);
                LOGGER.info("Repository successfully configured, uri : {}", uri);
            }
            else
            {
                LOGGER.warn("Repository already exists, uri : {}", uri);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void removeRepository(URI uri) throws Exception
    {
        lock.lock();
        try
        {
            LOGGER.info("Removing repository, uri : {}", uri);
            featuresService.removeRepository(uri);
            LOGGER.info("Repository removed successfully, uri : {}", uri);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void installFeatures(URI uri) throws Exception
    {
        lock.lock();
        try
        {
            final long time = System.currentTimeMillis();
            LOGGER.info("Installing features from repository, Uri : {} ", uri);
            final List<String> featureNames = configureAndGetFeatures(uri);
            internalInstallFeatures(featureNames);
            LOGGER.info("Successfully installed features from repository : {}, Time Taken : {}", uri, System.currentTimeMillis() - time);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void installFeatures(Collection<String> features) throws Exception
    {
        lock.lock();
        try
        {
            final long time = System.currentTimeMillis();
            internalInstallFeatures(features);
            LOGGER.info("Time Taken to install features : {}", System.currentTimeMillis() - time);
        }
        finally
        {
            lock.unlock();
        }

    }

    @Override
    public void updateDynamicFeatures() throws Exception
    {
        updateFeatures(dynamicRepositories);
    }

    @Override
    public void updateFeatures(Set<URI> uris) throws Exception
    {
        lock.lock();
        try
        {
            final long time = System.currentTimeMillis();
            for (URI uri : uris)
            {
                if(!repositoryExists(uri))
                {
                    LOGGER.error("Repository not exists, Configuring repository, uri : {} : ", uri);
                    addRepository(uri);
                }
            }
            LOGGER.info("Repository update started, uri : {}", uris);
            featuresService.refreshRepositories(uris);
            featuresService.updateFeaturesState(Map.of(), OPTIONS);
            LOGGER.info("Repository update completed, Time Taken : {}", System.currentTimeMillis() - time);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean isFeatureInstalled(String featureName) throws Exception
    {
        lock.lock();
        try
        {
            final Feature feature = featuresService.getFeature(featureName);
            return feature != null && featuresService.isInstalled(feature);
        }
        finally
        {
            lock.unlock();
        }
    }

    private List<String> configureAndGetFeatures(URI uri) throws Exception
    {
        if(!repositoryExists(uri))
        {
            LOGGER.error("Repository not exists, Configuring repository, uri : {} : ", uri);
            addRepository(uri);
        }

        final Repository repository = featuresService.getRepository(uri);
        final Feature[] features = repository.getFeatures();
        return Arrays.stream(features).map(Feature::getName).collect(Collectors.toList());
    }

    private void internalInstallFeatures(Collection<String> features) throws Exception
    {
        final Set<String> featureList = new LinkedHashSet<>();
        for (String featureName : features)
        {
            final Feature feature = featuresService.getFeature(featureName);
            if(feature != null)
            {
                final String featureId = feature.getId();
                if(featuresService.isInstalled(feature))
                {
                    LOGGER.error("Feature already installed, Feature : {} : ", featureId);
                }
                else
                {
                    featureList.add(feature.getId());
                }
            }
            else
            {
                LOGGER.error("Feature not exists, Feature : {} : ", featureName);
            }
        }
        if(!featureList.isEmpty())
        {
            featuresService.installFeatures(featureList, OPTIONS);
            for (String featureId: featureList)
            {
                LOGGER.info("{} feature installed", featureId);
            }
        }
    }
}
