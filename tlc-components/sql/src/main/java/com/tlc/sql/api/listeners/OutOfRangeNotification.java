package com.tlc.sql.api.listeners;


/**
 * @author Abishek
 * @version 1.0
 */
public interface OutOfRangeNotification
{
    default boolean processOutOfRangeNotification()
    {
        return false;
    }
}
