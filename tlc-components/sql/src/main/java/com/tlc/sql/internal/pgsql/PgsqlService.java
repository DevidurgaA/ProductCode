package com.tlc.sql.internal.pgsql;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheConfig;
import com.tlc.cache.CacheManager;
import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.service.Service;
import com.tlc.sql.SQLService;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.sql.api.sequence.SequenceGenerator;
import com.tlc.sql.internal.ds.AdminDataStoreImpl;
import com.tlc.sql.internal.ds.DsProvider;
import com.tlc.sql.internal.ds.OrgDataStoreImpl;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.internal.handler.DMLHandler;
import com.tlc.sql.internal.sequence.BatchSeqGeneratorImpl;
import com.tlc.sql.internal.sequence.SequenceGeneratorImpl;
import com.tlc.sql.internal.status.SQLErrorCodes;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author Abishek
 * @version 1.0
 */
public class PgsqlService implements SQLService, Service
{
    private DsProvider dsProvider;
    private AdminDataStore adminDatastore;

    private final DMLHandler dmlHandler;
    private final DDLHandler ddlHandler;

    private final Cache<Long, OrgDataStore> orgDataStoreCache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final Logger LOGGER = LoggerFactory.getLogger(PgsqlService.class);
    public PgsqlService()
    {
        final CacheConfig<Long, OrgDataStore> config = new CacheConfig<>();
        config.setExpireAfterAccess(TimeUnit.MINUTES.toMillis(10));
        this.orgDataStoreCache = CacheManager.getInstance().createCache(config);

        this.ddlHandler = new PgsqlDDLHandler();
        this.dmlHandler = new PgsqlDMLHandler();
    }

    @Override
    public void start(Map<String, String> input)
    {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName(Objects.requireNonNull(input.get("database.name")));
        final String[] servers = input.get("database.server").split(",");

        final int[] portNumbers = new int[servers.length];
        final String[] serverNames = new String[servers.length];
        int index = 0;
        for (String server : servers)
        {
            final String[] temp = server.split(":");
            serverNames[index] = temp[0];
            portNumbers[index++] = Integer.parseInt(temp[1]);
        }
        dataSource.setPortNumbers(portNumbers);
        dataSource.setServerNames(serverNames);

        final String userName = input.get("database.username");
        final String password = input.get("database.password");
        dataSource.setUser(userName);
        dataSource.setPassword(password);

        final HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");

        final String connectionPoolMax = input.get("connection.pool.max");
        if(connectionPoolMax != null)
        {
            config.setMaximumPoolSize(Integer.parseInt(connectionPoolMax));
        }
        final String connectionPoolMaxIdle = input.get("connection.pool.idle");
        if(connectionPoolMaxIdle != null)
        {
            config.setMinimumIdle(Integer.parseInt(connectionPoolMaxIdle));
        }
        final String connectionPoolIdleTimeout = input.get("connection.pool.idle.timeout");
        if(connectionPoolIdleTimeout != null)
        {
            config.setIdleTimeout(Integer.parseInt(connectionPoolIdleTimeout));
        }

        this.dsProvider = new DsProvider()
        {
            final HikariDataSource dataSource = new HikariDataSource(config);
            @Override
            public void close()
            {
                dataSource.close();
            }

            @Override
            public Connection getConnection() throws SQLException
            {
                return dataSource.getConnection();
            }

            @Override
            public boolean connect()
            {
                try (Connection ignored = getConnection())
                {
                    return true;
                }
                catch (Exception exp)
                {
                    return false;
                }
            }
        };
        if(!dsProvider.connect())
        {
            throw ErrorCode.get(SQLErrorCodes.DB_CONNECTION_FAILED);
        }
        this.adminDatastore = new AdminDataStoreImpl(dsProvider, dmlHandler, ddlHandler);
        LOGGER.info("Pgsql Service Activated");
    }

    @Override
    public void stop()
    {
        orgDataStoreCache.clear();
        if(dsProvider != null)
        {
            dsProvider.close();
        }
        adminDatastore = null;
        LOGGER.info("Pgsql Service deactivated");
    }

    @Override
    public void checkExtensions()
    {
        try(Connection connection = dsProvider.getConnection(); Statement statement = connection.createStatement())
        {
            statement.execute("CREATE EXTENSION IF NOT EXISTS CITEXT WITH SCHEMA pg_catalog");
            LOGGER.info("CITEXT extension verified successfully");
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_CONNECTION_FAILED, exp);
        }
    }

    @Override
    public OrgDataStore getOrgDataStore(Long orgId)
    {
        lock.readLock().lock();
        try
        {
            return orgDataStoreCache.computeIfAbsent(orgId, id -> new OrgDataStoreImpl(orgId, dsProvider, dmlHandler));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public AdminDataStore getAdminDataStore()
    {
        return adminDatastore;
    }

    @Override
    public SequenceGenerator getSequenceProvider(String sequenceName, boolean isBatch)
    {
        if (isBatch)
        {
            adminDatastore.initializeSequence(sequenceName, 100, 100);
            return new BatchSeqGeneratorImpl(sequenceName, adminDatastore);
        }
        else
        {
            adminDatastore.initializeSequence(sequenceName, 1, 1);
            return new SequenceGeneratorImpl(sequenceName, adminDatastore);
        }
    }
}
