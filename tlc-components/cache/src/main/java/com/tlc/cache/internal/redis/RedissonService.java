package com.tlc.cache.internal.redis;

import com.tlc.cache.Cache;
import com.tlc.cache.remote.CacheService;
import com.tlc.cache.remote.CacheConfig;
import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlc.commons.service.Service;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */
public class RedissonService implements CacheService, Service {

    private RedissonClient redisson;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonService.class);

    @Override
    public <K, V> Cache<K, V> createOrOpenCache(CacheConfig config) {
        final String name = config.getName();
        final RMapCache<K,V> map =  redisson.getMapCache(name);
        final int maxSize = config.getSize();

        if (maxSize != -1) {
            map.setMaxSize(maxSize);
        }

        return new RedissonMapWrapper<>(map, config.getTtl());
    }

    @Override
    public void start(Map<String, String> input) {
        final String type = input.get("cache.server.type");
        final String[] servers = input.get("cache.server").split(",");
        final String format = "redis://%s";
        final Config config = new Config();

        switch (type.toLowerCase())
        {
            case "single" -> config.useSingleServer().setAddress(String.format(format, servers[0]));
            case "cluster" -> {
                final ClusterServersConfig clusterServersConfig = config.useClusterServers();
                Arrays.stream(servers).map(item -> String.format(format, item)).forEach(clusterServersConfig::addNodeAddress);
            }
            case "sentinel" -> {
                final SentinelServersConfig sentinelServers = config.useSentinelServers();
                Arrays.stream(servers).map(item -> String.format(format, item)).forEach(sentinelServers::addSentinelAddress);
            }
            default -> throw ErrorCode.get(ErrorCodes.INVALID_DATA, type);
        }
        config.setCodec(new org.redisson.client.codec.StringCodec());
        this.redisson = Redisson.create(config);

        LOGGER.info("Cache Service Activated");
    }

    @Override
    public void stop() {
        LOGGER.info("Cache Service Deactivate - Entered");

        if (redisson != null) {
            redisson.shutdown();
        }

        LOGGER.info("Cache Service Deactivated");
    }
}
