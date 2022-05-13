package com.tlc.sql.api.meta;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.sequence.SequenceGenerator;
import com.tlc.sql.internal.status.SQLErrorCodes;


/**
 * @author Abishek
 * @version 1.0
 */
public class SequenceDefinition
{
    private final String sequenceName;
    private final int incrementBy;

    private SequenceGenerator sequenceGenerator;

    public static final String DATABASE_TABLE_PK_SEQUENCE_ID = "internal_sql_db_pk_%s";
    public SequenceDefinition(String sequenceName)
    {
        this(sequenceName, 1);
    }

    public SequenceDefinition(String seqGenerator, int incrementBy)
    {
        this.incrementBy = incrementBy;
        this.sequenceName = String.format(DATABASE_TABLE_PK_SEQUENCE_ID,
                seqGenerator.replaceAll("[^A-Za-z0-9]+", "_").toLowerCase());
    }

    public String getSequenceName()
    {
        return sequenceName;
    }

    public int getIncrementBy()
    {
        return incrementBy;
    }

    public SequenceGenerator getSequenceGenerator()
    {
        if(sequenceGenerator == null)
        {
            throw ErrorCode.getLite(SQLErrorCodes.DB_DATA_PK_SEQ_NOT_FOUND);
        }
        return sequenceGenerator;
    }

    public void setSequenceGenerator(SequenceGenerator generator)
    {
        if(sequenceGenerator != null)
        {
            throw ErrorCode.getLite(SQLErrorCodes.DB_SEQUENCE_ALREADY_INITIALIZED);
        }
        this.sequenceGenerator = generator;
    }
}
