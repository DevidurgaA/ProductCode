package com.tlc.web;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Abishek
 * @version 1.0
 */
class BundleListener extends com.tlc.commons.service.listener.BundleListener
{
    private final WebService webService;
    private final Map<String, Set<String>> loadedPaths;
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleListener.class);
    BundleListener(WebService webService)
    {
        this.webService = webService;
        this.loadedPaths = new HashMap<>();
    }

    @Override
    public void bundleStarted(Bundle bundle)
    {
        final Map<String, Action> actionMap = fetchActions(bundle);
        if (!actionMap.isEmpty())
        {
            actionMap.forEach(webService::register);
            LOGGER.info("Total Registered actions : {}, from Bundle : {}", actionMap.size(), bundle.getSymbolicName());
            loadedPaths.put(bundle.getSymbolicName(), Set.copyOf(actionMap.keySet()));
        }
    }

    @Override
    public void bundleStopped(Bundle bundle)
    {
        final Set<String> paths = loadedPaths.remove(bundle.getSymbolicName());
        if(paths != null)
        {
            paths.forEach(webService::unregister);
            LOGGER.info("Total Unregistered actions : {}, from Bundle : {}", paths.size(), bundle.getSymbolicName());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Action> fetchActions(Bundle bundle)
    {
        try
        {
            final Map<String, Action> actionsMap = new HashMap<>();
            final Enumeration<URL> enumeration = bundle.findEntries("/", "*.class", true);
            if(enumeration != null)
            {
                while (enumeration.hasMoreElements())
                {
                    final URL url = enumeration.nextElement();
                    final String file = url.getFile();
                    if(file.contains("/action/"))
                    {
                        final int indexOfClass = file.indexOf(".class");
                        final String className = file.substring((file.startsWith("/") ? 1 : 0), indexOfClass).replace("/", ".");
                        final Class<?> classObj = bundle.loadClass(className);
                        final WebAction annotation = classObj.getAnnotation(WebAction.class);
                        if(annotation != null)
                        {
                            final String path = annotation.path();
                            final Constructor<Action> constructor = (Constructor<Action>) classObj.getConstructor();
                            final Action action = constructor.newInstance();
                            actionsMap.put(path, action);
                        }
                    }
                }
            }
            return actionsMap;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, exp);
        }
    }
}
