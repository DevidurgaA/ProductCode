package com.tlc.sql;

import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.sql.api.sequence.SequenceGenerator;

/**
 * @author Abishek
 * @version 1.0
 */
public interface SQLService
{
    void checkExtensions();

    OrgDataStore getOrgDataStore(Long orgId);

    AdminDataStore getAdminDataStore();

    SequenceGenerator getSequenceProvider(String sequenceName, boolean isBatch);
}
