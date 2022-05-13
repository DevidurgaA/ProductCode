package com.tlc.sql.internal.sequence;

import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.sequence.SequenceGenerator;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class SequenceGeneratorImpl implements SequenceGenerator
{
    private final String sequenceName;
    protected final AdminDataStore dataStore;

    public SequenceGeneratorImpl(String sequenceName, AdminDataStore dataStore)
    {
        this.dataStore = Objects.requireNonNull(dataStore);
        this.sequenceName = Objects.requireNonNull(sequenceName);
        @SuppressWarnings("unused")
        final long nextNumber = getNextNumber();
    }

    @Override
    public long getNextNumber()
    {
        return dataStore.getSequenceNextValue(sequenceName);
    }
}
