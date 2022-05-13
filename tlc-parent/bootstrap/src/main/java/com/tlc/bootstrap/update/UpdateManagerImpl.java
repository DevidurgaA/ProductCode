package com.tlc.bootstrap.update;

import com.tlc.bootstrap.FeatureManager;
import com.tlc.bootstrap.Bootstrap;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "system.update")
public class UpdateManagerImpl implements UpdateManager
{
    @Reference
    private Bootstrap bootstrap;

    @Reference
    private Scheduler scheduler;

    private AtomicReference<UpdateMode> updateMode;

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdateManager.class);

    @Activate
    public void activate(Map<String, String> input) throws Exception
    {
        final String mode = input.get("mode");
        final UpdateMode updateMode = UpdateMode.get(mode);
        this.updateMode = new AtomicReference<>(updateMode);
        final String cronExpression = input.get("expression");

        LOGGER.info("Configuring UpdateManager with update mode : {}, Cron Expression : {}", mode, cronExpression);
        if(this.updateMode.get() == UpdateMode.AUTO)
        {
            scheduler.schedule((Runnable) this::updateRepositories,
                    scheduler.EXPR(cronExpression).canRunConcurrently(false).name("system.update.auto"));
        }
    }

    @Modified
    public void modified(Map<String, String> input) throws Exception
    {
        final String mode = input.get("mode");
        final String cronExpression = input.get("expression");
        LOGGER.info("Reconfiguring UpdateManager with update mode : {}, Cron Expression : {}", mode, cronExpression);

        final UpdateMode oldMode = this.updateMode.get();
        final UpdateMode currentMode = UpdateMode.get(mode);
        this.updateMode = new AtomicReference<>(currentMode);
        if(oldMode == UpdateMode.AUTO)
        {
            if(currentMode == UpdateMode.AUTO)
            {
                scheduler.reschedule("system.update.auto",
                        scheduler.EXPR(cronExpression).canRunConcurrently(false).name("system.update.auto"));
            }
        }
        else if(currentMode == UpdateMode.AUTO)
        {
            scheduler.schedule((Runnable) this::updateRepositories,
                    scheduler.EXPR(cronExpression).canRunConcurrently(false).name("system.update.auto"));
        }
    }

    @Deactivate
    public void deactivate()
    {
        this.updateMode = null;
        scheduler.unschedule("system.update.auto");
    }

    @Override
    public void update()
    {
        try
        {
            final UpdateMode updateMode = this.updateMode.get();
            if(updateMode == UpdateMode.AUTO)
            {
                LOGGER.info("Auto-Update Job Triggered");
                scheduler.trigger("system.update.auto");
            }
            else if(updateMode != UpdateMode.MANUAL)
            {
                LOGGER.info("Manual-Update Job Triggered");
                scheduler.schedule((Runnable) this::updateRepositories, scheduler.NOW());
            }
            else
            {
                LOGGER.error("Update mode is off");
            }
        }
        catch (Exception exp)
        {
            LOGGER.error("Update Job schedule failed, reason :",exp);
        }
    }

    private void updateRepositories()
    {
        if(bootstrap.isAppInitialized())
        {
            try
            {
                LOGGER.info("Update task started");
                final FeatureManager featureManager = bootstrap.getFeatureManager();
                featureManager.updateDynamicFeatures();
                LOGGER.info("Update task completed");
            }
            catch (Exception exception)
            {
                LOGGER.error("Update task failed, reason :", exception);
            }
        }
        else
        {
            LOGGER.warn("Application not initialized, ignoring update request");
        }
    }
}
