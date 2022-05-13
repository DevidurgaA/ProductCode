package com.tlc.bootstrap;

import java.net.URI;
import java.util.Collection;
import java.util.Set;


public interface FeatureManager
{
    boolean repositoryExists(URI repository) throws Exception;

    void addRepository(URI repository) throws Exception;

    void removeRepository(URI uri) throws Exception;

    boolean isFeatureInstalled(String feature) throws Exception;

    void installFeatures(URI uri) throws Exception;

    void installFeatures(Collection<String> features) throws Exception;

    void updateFeatures(Set<URI> uris) throws Exception;

    void updateDynamicFeatures() throws Exception;

}
