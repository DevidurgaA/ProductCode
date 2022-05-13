package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.DDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.PKDefinition;
import com.tlc.sql.api.meta.SequenceDefinition;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class DropTable implements DDLAction
{
    private final TableDefinition tableDefinition;
    public DropTable(TableDefinition tableDefinition)
    {
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getDropTableSQL(tableDefinition);
        final PKDefinition pkDefinition = tableDefinition.getPkDefinition();
        if(pkDefinition.hasSequenceGenerator())
        {
            final SequenceDefinition sequenceDefinition = pkDefinition.getSequenceDefinition();
            final String sequenceName = sequenceDefinition.getSequenceName();
            final String seqSql = ddlHandler.getDropSequenceSQL(sequenceName);
            return new String[] { sql, seqSql };
        }
        else
        {
            return new String[] { sql };
        }
    }
}
