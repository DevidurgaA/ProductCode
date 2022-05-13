package com.tlc.cache;

import com.tlc.cache.internal.redis.RedissonService;
import com.tlc.cache.remote.CacheService;
import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.commons.util.ConfLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Sundar
 * @version 1.0
 */
public final class CacheAccess implements BundleActivator {

    private static final AtomicReference<CacheService> REFERENCE = new AtomicReference<>();

    private static void register(CacheService cacheService) {
        if (!REFERENCE.compareAndSet(null, cacheService)) {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service already initialized");
        }
    }

    private static CacheService unregister() {
        return REFERENCE.getAndSet(null);
    }

    public static CacheService get() {
        final CacheService cacheService = REFERENCE.get();

        if(cacheService == null) {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service not initialized");
        } else {
            return cacheService;
        }
    }

    @Override
    public void start(BundleContext bundleContext) throws IOException {
        final RedissonService cacheService = new RedissonService();
        final Map<String, String> config = ConfLoader.load("cache.redis.cfg");

        cacheService.start(config);
        register(cacheService);
    }

    @Override
    public void stop(BundleContext bundleContext) {
        final RedissonService cacheService = (RedissonService)unregister();
        cacheService.stop();
    }
}
