package com.tlc.commons.service;

import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */
public interface Service
{
    void start(Map<String, String> input);

    void stop();
}
