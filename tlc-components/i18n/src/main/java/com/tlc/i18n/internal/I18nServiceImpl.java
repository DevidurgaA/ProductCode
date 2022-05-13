package com.tlc.i18n.internal;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheManager;
import com.tlc.commons.service.Service;
import com.tlc.i18n.I18nResolver;
import com.tlc.i18n.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */

public class I18nServiceImpl implements I18nService, Service
{
    private final Cache<Locale, I18nResolverImpl> resolvers;
    private static final Logger LOGGER = LoggerFactory.getLogger(I18nServiceImpl.class);
    public I18nServiceImpl()
    {
        this.resolvers = CacheManager.getInstance().createCache();
    }

    @Override
    public void start(Map<String, String> input)
    {
        LOGGER.info("I18n Service Activated");
    }

    @Override
    public void stop()
    {
        resolvers.clear();
        LOGGER.info("I18n Service deactivated");
    }

    @Override
    public I18nResolver getResolver(Locale locale)
    {
        return resolvers.computeIfAbsent(locale, k -> new I18nResolverImpl(locale));
    }
}
