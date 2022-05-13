package com.tlc.sql;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.commons.util.ConfLoader;
import com.tlc.commons.util.Env;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.internal.meta.MetaDataHandler;
import com.tlc.sql.internal.pgsql.PgsqlService;
import com.tlc.sql.internal.update.DDLUpdateManager;
import com.tlc.sql.internal.update.DMLUpdateManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Abishek
 * @version 1.0
 */
public final class SQLAccess implements BundleActivator
{
    private static final AtomicReference<SQLService> REFERENCE = new AtomicReference<>();

    private static void register(SQLService service)
    {
        if(!REFERENCE.compareAndSet(null, service))
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service already initialized");
        }
    }

    private static SQLService unregister()
    {
        return REFERENCE.getAndSet(null);
    }

    public static SQLService get()
    {
        final SQLService service = REFERENCE.get();
        if(service == null)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_ACCESS, "Service not initialized");
        }
        else
        {
            return service;
        }
    }

    private BundleListener bundleListener;

    @Override
    public void start(BundleContext bundleContext) throws IOException
    {
        final PgsqlService sqlService = new PgsqlService();
        final Map<String, String> input = ConfLoader.load("database.pgsql.cfg");
        sqlService.start(input);

        final AdminDataStore adminDataStore = sqlService.getAdminDataStore();
        final MetaDataHandler metaDataHandler = new MetaDataHandler(adminDataStore);
        if (Env.isDevMode())
        {
            this.bundleListener = new SQLDevListener(metaDataHandler, new DDLUpdateManager(adminDataStore),
                    new DMLUpdateManager(metaDataHandler, adminDataStore));
            sqlService.checkExtensions();
        }
        else
        {
            this.bundleListener = new BundleListener(metaDataHandler);
        }
        bundleContext.addBundleListener(bundleListener);
        register(sqlService);
    }

    @Override
    public void stop(BundleContext bundleContext)
    {
        final PgsqlService sqlService = (PgsqlService)unregister();

        bundleContext.removeBundleListener(bundleListener);

        sqlService.stop();
    }
}
