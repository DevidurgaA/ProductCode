package com.tlc.bootstrap;


public interface Bootstrap
{
    String getVersion();

    boolean isAppInitialized();

    FeatureManager getFeatureManager();
}
