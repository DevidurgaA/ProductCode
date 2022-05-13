package com.tlc.commons.util;

import java.io.File;
import java.lang.Boolean;
import java.lang.System;

/**
 * <p>
 * Provides utility functionalities to handle a bundle's meta data
 * </p>
 *
 * @author Selvakumar G
 * @version 1.0
 */
public final class Env
{
    private static final String KARAF_HOME = System.getProperty("karaf.home");
    private static final boolean IS_DEV_MODE = Boolean.parseBoolean(System.getProperty("tlc.dev.mode"));

    private static final String WORK_DIRECTORY = KARAF_HOME + File.separator + "data" + File.separator + "tlc";
    private static final String CONF_DIRECTORY = KARAF_HOME + File.separator + "conf";

    /**
     * <p>
     * Checks whether development mode enabled or not
     * </p>
     *
     * @return
     */
    public static Boolean isDevMode()
    {
        return IS_DEV_MODE;
    }

    /**
     * <p>
     * Returns TLC Home directory location
     * </p>
     *
     * @return
     */
    public static String getWorkDirectory()
    {
        return WORK_DIRECTORY;
    }

    public static String getConfDirectory()
    {
        return CONF_DIRECTORY;
    }

    public static String getServerHome()
    {
        return KARAF_HOME;
    }
}
