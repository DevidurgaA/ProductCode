package com.tlc.web;


/**
 * @author Abishek
 * @version 1.0
 */
public interface WebService
{
    void register(String path, Action action);

    void unregister(String path);
}
