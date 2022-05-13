package com.tlc.sql;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.util.Env;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.meta.MetaDataHandler;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * By utilizing the bundle lifecycle hooks, it will perform the actions like meta data parsing etc...
 * </p>
 *
 * @author Selvakumar G
 * @version 1.0
 */
class BundleListener extends com.tlc.commons.service.listener.SortedBundleListener
{

    protected static final String SNAPSHOTS_HOME = Env.getWorkDirectory() + File.separator + "sql" + File.separator + "snapshot";

    protected static final String SQL_FILES_DIR = "/resources/sql";
    protected static final String META_FILE_NAME = "meta.xml";

    private final MetaDataHandler metaDataHandler;

    protected final Map<String, Set<String>> loadedTables;
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleListener.class);
    BundleListener(MetaDataHandler metaDataHandler)
    {
        this.loadedTables = new HashMap<>();
        this.metaDataHandler = Objects.requireNonNull(metaDataHandler);
    }

    /**
     * <p>
     * Process the metadata files on bundle started event
     * </p>
     *
     * @param bundle
     */
    @Override
    public void bundleStarted(final Bundle bundle)
    {
        final String symbolicName = bundle.getSymbolicName();
        final URL sqlUrl = bundle.getResource(BundleListener.SQL_FILES_DIR + "/" + META_FILE_NAME);
        if (sqlUrl != null)
        {
            try(InputStream inputStream = sqlUrl.openStream())
            {
                final Map<String, TableDefinition> tableDefinitions = metaDataHandler.fetchTableDefinitions(inputStream);
                metaDataHandler.loadMetaData(tableDefinitions);
                loadedTables.put(symbolicName, Set.copyOf(tableDefinitions.keySet()));
                LOGGER.info("Meta files loaded successfully : {}, Total tables : {}", symbolicName, tableDefinitions.size());
            }
            catch (IOException e)
            {
                throw ErrorCode.get(SQLErrorCodes.BUNDLE_INVALID_META_FILE);
            }
        }
        else
        {
            LOGGER.info("No meta files found : {}", symbolicName);
        }
    }

    /**
     * <p>
     * Unloads the metadata files on bundle stopped event
     * </p>
     *
     * @param bundle
     */
    @Override
    public void bundleStopped(final Bundle bundle)
    {
        final String symbolicName = bundle.getSymbolicName();
        final Set<String> tables = loadedTables.get(symbolicName);
        if (tables != null)
        {
            metaDataHandler.unloadMetaData(tables);
            LOGGER.info("Meta data unloaded successfully : {}, Total tables : {}", symbolicName, tables.size());
        }
    }
}
