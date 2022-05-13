package com.tlc.sql.internal.update;

import com.tlc.sql.api.DataContainer;

/**
 * @author Abishek
 * @version 1.0
 */
public interface PreCommitCallback
{
    void call(DataContainer dataContainer);
}
