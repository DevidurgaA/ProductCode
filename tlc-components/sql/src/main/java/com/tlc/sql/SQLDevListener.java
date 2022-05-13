package com.tlc.sql;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.commons.util.Env;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.meta.MetaDataHandler;
import com.tlc.sql.internal.status.SQLErrorCodes;
import com.tlc.sql.internal.update.DDLUpdateManager;
import com.tlc.sql.internal.update.DMLUpdateManager;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Abishek
 * @version 1.0
 */
public class SQLDevListener extends BundleListener
{
    private static final String SNAPSHOTS_HISTORY_LOCATION = SNAPSHOTS_HOME + File.separator + "meta-history.json";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    protected static final String DATA_FILE_NAME = "data.xml";

    private final DDLUpdateManager ddlUpdateManager;
    private final DMLUpdateManager dmlUpdateManager;

    private final MetaDataHandler metaDataHandler;
    private final JsonObject historyRecord;

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLDevListener.class);
    SQLDevListener(MetaDataHandler metaDataHandler, DDLUpdateManager ddlUpdateManager, DMLUpdateManager dmlUpdateManager)
    {
        super(metaDataHandler);
        this.historyRecord = fetchMetaFileHistory();
        this.metaDataHandler = Objects.requireNonNull(metaDataHandler);
        this.dmlUpdateManager = Objects.requireNonNull(dmlUpdateManager);
        this.ddlUpdateManager = Objects.requireNonNull(ddlUpdateManager);
    }

    @Override
    public synchronized void bundleStarted(final Bundle bundle)
    {
        final long time = System.currentTimeMillis();

        final String serverHome = Env.getServerHome();
        final URI serverHomeUri = new File(serverHome).toURI();
        final String symbolicName = bundle.getSymbolicName();
        final JsonObject bundleHistory = historyRecord.optJsonObject(symbolicName);
        if(bundleHistory == null)
        {
            final long currentTime = bundle.getLastModified();
            final File metaFile = discoverSQLFileFromBundle(bundle, META_FILE_NAME);
            final File dataFile = discoverSQLFileFromBundle(bundle, DATA_FILE_NAME);

            final JsonObject newHistoryRecord = Json.object();

            if(metaFile != null)
            {
                populateMetaData(symbolicName, metaFile);

                newHistoryRecord.put("modified", currentTime);
                newHistoryRecord.put("meta", serverHomeUri.relativize(metaFile.toURI()).getPath());
                historyRecord.put(symbolicName, newHistoryRecord);
                writeMetaFileHistory();
            }
            if(dataFile != null)
            {
                LOGGER.info("Datafile detected from bundle : {}", symbolicName);
                metaDataHandler.populateData(List.of(dataFile), Map.of());
                LOGGER.info("Metafile populated successfully from bundle : {}", symbolicName);

                newHistoryRecord.put("modified", currentTime);
                newHistoryRecord.put("data", serverHomeUri.relativize(dataFile.toURI()).getPath());
                historyRecord.put(symbolicName, newHistoryRecord);
                writeMetaFileHistory();
            }
        }
        else
        {
            final long existingTime = bundleHistory.getLong("modified");
            final long currentTime = bundle.getLastModified();

            if (currentTime == existingTime)
            {
                LOGGER.info("No changes detected on bundle {}", symbolicName);
                final String existingMetaFilePath = bundleHistory.optString("meta", null);
                if(existingMetaFilePath != null)
                {
                    final File existingMetaFile = new File(serverHome, existingMetaFilePath);
                    loadMetaData(symbolicName, existingMetaFile);
                }
            }
            else
            {
                LOGGER.info("Updated bundle detected : {}", symbolicName);
                final String existingMetaFilePath = bundleHistory.optString("meta", null);
                final String existingDataFilePath = bundleHistory.optString("data", null);

                final File metaFile = discoverSQLFileFromBundle(bundle, META_FILE_NAME);
                final File dataFile = discoverSQLFileFromBundle(bundle, DATA_FILE_NAME);

                if(metaFile != null)
                {
                    LOGGER.info("Found Meta file in bundle : {}", symbolicName);
                    if(existingMetaFilePath != null)
                    {
                        final File existingMetaFile = new File(serverHome, existingMetaFilePath);
                        final Map<String, TableDefinition> currentTableDef = metaDataHandler.fetchTableDefinitions(existingMetaFile);
                        final Map<String, TableDefinition> newTableDef = metaDataHandler.fetchTableDefinitions(metaFile);
                        updateMetaData(symbolicName, currentTableDef, newTableDef);
                    }
                    else
                    {
                        populateMetaData(symbolicName, metaFile);
                    }
                    bundleHistory.put("modified", currentTime);
                    bundleHistory.put("meta", serverHomeUri.relativize(metaFile.toURI()).getPath());
                    writeMetaFileHistory();
                }
                else if(existingMetaFilePath != null)
                {
                    final File existingMetaFile = new File(serverHome, existingMetaFilePath);
                    final Map<String, TableDefinition> currentTableDef = metaDataHandler.fetchTableDefinitions(existingMetaFile);
                    updateMetaData(symbolicName, currentTableDef, Map.of());
                    bundleHistory.remove("meta");
                    writeMetaFileHistory();
                }


                if(dataFile != null)
                {
                    LOGGER.info("Found Data file in bundle : {}", symbolicName);
                    if(existingDataFilePath != null)
                    {
                        final File existingDataFile = new File(serverHome, existingDataFilePath);
                        dmlUpdateManager.doDMLUpdate(List.of(existingDataFile), List.of(dataFile), Set.of(), Map.of());
                        LOGGER.info("SQLData updated successfully from bundle : {}", symbolicName);
                    }
                    else
                    {
                        LOGGER.info("Datafile detected from bundle : {}", symbolicName);
                        metaDataHandler.populateData(List.of(dataFile), Map.of());
                        LOGGER.info("Metafile populated successfully from bundle : {}", symbolicName);
                    }
                    bundleHistory.put("modified", currentTime);
                    bundleHistory.put("data", serverHomeUri.relativize(dataFile.toURI()).getPath());
                    writeMetaFileHistory();
                }
                else if(existingDataFilePath != null)
                {

                    final File existingDataFile = new File(serverHome, existingDataFilePath);
                    dmlUpdateManager.doDMLUpdate(List.of(existingDataFile), List.of(), Set.of(), Map.of());

                    bundleHistory.remove("data");
                    writeMetaFileHistory();

                    LOGGER.info("SQLData updated(removed) successfully from bundle : {}", symbolicName);
                }
            }
        }
        LOGGER.info("Time taken to process sql data from bundle {} : {}", symbolicName, System.currentTimeMillis() - time);
    }

    private void updateMetaData(String symbolicName, Map<String, TableDefinition> oldC, Map<String, TableDefinition> newC)
    {
        LOGGER.info("Going to update metadata from bundle : {}", symbolicName);

        ddlUpdateManager.doPreSchemaUpdate(oldC, newC);
        ddlUpdateManager.doPostSchemaUpdate(oldC, newC);

        metaDataHandler.loadMetaData(newC);
        loadedTables.put(symbolicName, Set.copyOf(newC.keySet()));

        LOGGER.info("Metadata updated successfully from bundle : {}", symbolicName);
    }

    private void populateMetaData(String symbolicName, File file)
    {
        LOGGER.info("Going to populate metadata from bundle: {}", symbolicName);
        final Map<String, TableDefinition> definitions = metaDataHandler.fetchTableDefinitions(file);
        metaDataHandler.populateMetaData(definitions);
        loadedTables.put(symbolicName, Set.copyOf(definitions.keySet()));
        LOGGER.info("Metafile populated successfully from bundle: {}, Total tables : {}", symbolicName, definitions.size());
    }

    private void loadMetaData(String symbolicName, File file)
    {
        LOGGER.info("Going to load metadata from bundle: {}", symbolicName);
        final Map<String, TableDefinition> definitions = metaDataHandler.fetchTableDefinitions(file);
        metaDataHandler.loadMetaData(definitions);
        loadedTables.put(symbolicName, Set.copyOf(definitions.keySet()));
        LOGGER.info("Metafile loaded successfully from bundle: {}, Total tables : {}", symbolicName, definitions.size());
    }

    private File discoverSQLFileFromBundle(final Bundle bundle, String name) throws ErrorCode
    {
        final URL sqlUrl = bundle.getResource(BundleListener.SQL_FILES_DIR + "/" + name);
        if (sqlUrl != null)
        {
            try
            {
                final long bundleId = bundle.getBundleId();
                final long modified = bundle.getLastModified();

                final LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(modified), ZoneOffset.UTC);
                final String zdtString = TIME_FORMATTER.format(dateTime);
                final String filePath = SNAPSHOTS_HOME + File.separator + bundleId + File.separator + zdtString + File.separator + name;
                final Path path = Paths.get(filePath);
                if (!Files.exists(path))
                {
                    Files.createDirectories(path.getParent());
                    try(InputStream in = sqlUrl.openStream())
                    {
                        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                return path.toFile();
            }
            catch (IOException e)
            {
                throw ErrorCode.get(SQLErrorCodes.BUNDLE_INVALID_META_FILE);
            }
            catch (Exception e)
            {
                throw ErrorCode.get(SQLErrorCodes.BUNDLE_META_FILE_READ_ACTION_FAILED, e);
            }
        }
        return null;
    }

    private void writeMetaFileHistory() throws ErrorCode
    {
        final Path path = Path.of(SNAPSHOTS_HISTORY_LOCATION);
        try
        {
            if(!Files.exists(path.getParent()))
            {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, historyRecord.getBytes());
        }
        catch (IOException e)
        {
            throw ErrorCode.get(SQLErrorCodes.BUNDLE_META_FILE_HISTORY_READ_ACTION_FAILED, e);
        }
    }

    private JsonObject fetchMetaFileHistory() throws ErrorCode
    {
        final File metaFilesHistoryRecord = new File(SNAPSHOTS_HISTORY_LOCATION);
        if (metaFilesHistoryRecord.exists())
        {
            try (final FileInputStream fileInputStream = new FileInputStream(metaFilesHistoryRecord))
            {
                return Json.object(fileInputStream);
            }
            catch (FileNotFoundException e)
            {
                throw ErrorCode.get(SQLErrorCodes.BUNDLE_META_FILE_HISTORY_NOT_FOUND, e);
            }
            catch (IOException e)
            {
                throw ErrorCode.get(SQLErrorCodes.BUNDLE_META_FILE_HISTORY_READ_ACTION_FAILED, e);
            }
        }
        return Json.object();
    }
}
