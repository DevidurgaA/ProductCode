package com.tlc.sql.internal.handler;

import com.tlc.sql.api.meta.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author Abishek
 * @version 1.0
 */
public abstract class AbstractDDLHandler implements DDLHandler
{
    @Override
    public String getInitializeSequenceSQL(String sequenceName, int start, int incrementBy)
    {
        return "CREATE SEQUENCE IF NOT EXISTS \""+sequenceName+"\" START "+start+" INCREMENT BY "+incrementBy;
    }

    @Override
    public String getCreateSequenceSQL(String sequenceName, int start, int incrementBy)
    {
        return "CREATE SEQUENCE \""+sequenceName+"\" START "+start+" INCREMENT BY "+incrementBy;
    }

    @Override
    public String getUpdateSequenceSQL(String sequenceName, int incrementBy)
    {
        return "ALTER SEQUENCE \""+sequenceName+"\" INCREMENT BY "+incrementBy;
    }

    @Override
    public String getDropSequenceSQL(String sequenceName)
    {
        return "DROP SEQUENCE \""+sequenceName+"\"";
    }

    @Override
    public String[] getCreateTableSQL(TableDefinition tableDefinition)
    {
        final List<String> sqlStatements = new LinkedList<>();
        final StringBuilder createBuffer = new StringBuilder(750);

        final String convertedTableName = getTableName(tableDefinition);
        createBuffer.append("CREATE TABLE ");
        createBuffer.append(convertedTableName);
        createBuffer.append(" ( ");

        final Map<String, ColumnDefinition> definitions = tableDefinition.getColumnDefinition();
        for (ColumnDefinition columnDefinition : definitions.values())
        {
            final String columnSQL = getColumnSQL(columnDefinition);
            createBuffer.append(columnSQL).append(", ");
        }

        final PKDefinition pkDefinition = tableDefinition.getPkDefinition();
        final String primaryKeySQL = getPrimaryKeySQL(pkDefinition);
        createBuffer.append(primaryKeySQL);

        final Map<String, FKDefinition> fkDefinitions = tableDefinition.getFKDefinitions();
        if(fkDefinitions.size() > 0)
        {
            for(FKDefinition fkDefinition : fkDefinitions.values())
            {
                createBuffer.append(", ");
                final String foreignKeySQL = getForeignKeySQL(fkDefinition);
                createBuffer.append(foreignKeySQL);
            }
        }
        final Map<String, UKDefinition> ukDefinitions = tableDefinition.getUKDefinitions();
        if(ukDefinitions.size() > 0)
        {
            for(UKDefinition ukDefinition : ukDefinitions.values())
            {
                createBuffer.append(", ");
                final String uniqueKeySQL = getUniqueKeySQL(ukDefinition);
                createBuffer.append(uniqueKeySQL);
            }
        }
        createBuffer.append(")");

        sqlStatements.add(createBuffer.toString());

        final Map<String, IndexDefinition> indexDefinitions = new HashMap<>(tableDefinition.getIndexDefinitions());
        if(indexDefinitions.size() > 0)
        {
            for(IndexDefinition indexDefinition : indexDefinitions.values())
            {
                final String indexSQL = getIndexSQL(convertedTableName, indexDefinition);
                sqlStatements.add(indexSQL);
            }
        }

        if(pkDefinition.hasSequenceGenerator())
        {
            final SequenceDefinition sequenceDefinition = pkDefinition.getSequenceDefinition();
            final int incrementBy = sequenceDefinition.getIncrementBy();
            final String seqName = sequenceDefinition.getSequenceName();
            final String seqSql = getCreateSequenceSQL(seqName, incrementBy, incrementBy);
            sqlStatements.add(seqSql);
        }
        return sqlStatements.toArray(new String[0]);
    }

    @Override
    public String getAddColumnSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        final StringBuilder alterBuilder = new StringBuilder();
        alterBuilder.append("ALTER TABLE ").append(getTableName(tableDefinition)).append(" ADD ");
        final String columnSQL = getColumnSQL(columnDefinition);
        alterBuilder.append(columnSQL);
        return alterBuilder.toString();
    }

    @Override
    public String getAddForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition)
    {
        final StringBuilder alterBuilder = new StringBuilder();
        alterBuilder.append("ALTER TABLE ").append(getTableName(tableDefinition)).append(" ADD ");
        final String foreignKeySQL = getForeignKeySQL(fkDefinition);
        alterBuilder.append(foreignKeySQL);
        return alterBuilder.toString();
    }

    @Override
    public String getAddUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition)
    {
        final StringBuilder alterBuilder = new StringBuilder();
        alterBuilder.append("ALTER TABLE ").append(getTableName(tableDefinition)).append(" ADD ");
        final String uniqueKeySQL = getUniqueKeySQL(ukDefinition);
        alterBuilder.append(uniqueKeySQL);
        return alterBuilder.toString();
    }

    @Override
    public String getAddIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition)
    {
        final String convertedTableName = getTableName(tableDefinition);
        return getIndexSQL(convertedTableName, indexDefinition);
    }

    @Override
    public String getDropColumnSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        return "ALTER TABLE " + getTableName(tableDefinition) +
                " DROP COLUMN " + getColumnName(columnDefinition);
    }

    @Override
    public String getDropForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition)
    {
        return "ALTER TABLE " + getTableName(tableDefinition) +
                " DROP CONSTRAINT " + fkDefinition.getName();
    }

    @Override
    public String getDropIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition)
    {
        return "DROP INDEX " + indexDefinition.getName();
    }

    @Override
    public String getDropUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition)
    {
        return "ALTER TABLE " + getTableName(tableDefinition) +
                " DROP CONSTRAINT " + ukDefinition.getName();
    }

    @Override
    public String getDropTableSQL(TableDefinition tableDefinition)
    {
        return "DROP TABLE "+getTableName(tableDefinition);
    }

    @Override
    public String[] getUpdateColumnLengthSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        return getChangeColumnDataTypeSQL(tableDefinition, columnDefinition);
    }

    /**
     * {@inheritDoc}
     *
     * @param tableDefinition Table definition
     * @param columnDefinition Column definition
     * @return The query to alter the decimal data type weight
     */
    @Override
    public String[] getIncreaseDecimalWeightSQL(final TableDefinition tableDefinition,
                                                final ColumnDefinition columnDefinition) {
        return getChangeColumnDataTypeSQL(tableDefinition, columnDefinition);
    }

    @Override
    public String[] getChangeColumnDataTypeSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        return new String[] { "ALTER TABLE " + getTableName(tableDefinition) + " ALTER COLUMN " + getColumnName(columnDefinition)
                + " " + getDataType(columnDefinition, columnDefinition.getMaxLength()) };
    }

    @Override
    public String getChangeColumnNotNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        return "ALTER TABLE " + getTableName(tableDefinition) + " ALTER COLUMN " + getColumnName(columnDefinition)
                + " " + getDataType(columnDefinition, columnDefinition.getMaxLength()) +" NOT NULL";
    }

    @Override
    public String getChangeColumnNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        return "ALTER TABLE " + getTableName(tableDefinition) + " ALTER COLUMN " + getColumnName(columnDefinition)
                + " " + getDataType(columnDefinition, columnDefinition.getMaxLength()) +" NULL";
    }

    @Override
    public String[] getUpdateForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition)
    {
        final String tableName = getTableName(tableDefinition);
        return new String[] { "ALTER TABLE " + tableName +" DROP CONSTRAINT " + fkDefinition.getName(),
                "ALTER TABLE " + tableName +" ADD "+ getForeignKeySQL(fkDefinition) };
    }

    @Override
    public String[] getUpdateIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition)
    {
        final String tableName = getTableName(tableDefinition);
        return new String[] { "DROP INDEX " + indexDefinition.getName(),
                             getIndexSQL(tableName, indexDefinition) };
    }

    @Override
    public String[] getUpdateUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition)
    {
        final String tableName = getTableName(tableDefinition);
        return new String[] { "ALTER TABLE " + tableName + " DROP CONSTRAINT " + ukDefinition.getName(),
                "ALTER TABLE " + tableName +" ADD "+ getUniqueKeySQL(ukDefinition)};
    }

    protected String getTableName(TableDefinition tableDefinition)
    {
        return DDLDMLUtil.getTableName(tableDefinition);
    }

    protected String getColumnName(ColumnDefinition columnDefinition)
    {
        return DDLDMLUtil.getColumnName(columnDefinition);
    }

    protected String getColumnName(String column)
    {
        return DDLDMLUtil.getColumnName(column);
    }

    protected String getFKConstraint(FKDefinition.FKConstraint constraint)
    {
        if(constraint == FKDefinition.FKConstraint.ON_DELETE_CASCADE)
        {
            return "ON DELETE CASCADE";
        }
        else
        {
            return "ON DELETE RESTRICT";
        }
    }

    protected String getColumnSQL(ColumnDefinition definition)
    {
        return getColumnName(definition) + " " + getDataType(definition, definition.getMaxLength())
                + (definition.isNullable() ? " NULL " : " NOT NULL ");
    }

    protected String getIndexSQL(String convertedTableName, IndexDefinition indexDefinition)
    {
        return "CREATE INDEX " + indexDefinition.getName() +  " ON " + convertedTableName +
                indexDefinition.getColumns().stream().map(this::getColumnName).collect(Collectors.joining(",", "(", ")"));
    }

    private String getPrimaryKeySQL(PKDefinition pkDefinition)
    {
        return "CONSTRAINT " + pkDefinition.getKeyName() + " PRIMARY KEY (" + getColumnName(pkDefinition.geColumnName()) + ")";
    }

    private String getUniqueKeySQL(UKDefinition ukDefinition)
    {
        return "CONSTRAINT " + ukDefinition.getName() +  " UNIQUE " +
                ukDefinition.getColumns().stream().map(this::getColumnName).collect(Collectors.joining(",", "(", ")"));
    }

    private String getForeignKeySQL(FKDefinition fKDefinition)
    {
        return "CONSTRAINT " + fKDefinition.getName() +
                " FOREIGN KEY (" + getColumnName(fKDefinition.getLocal().getColumnName()) + ")" +
                " REFERENCES " + getTableName(fKDefinition.getReferenceTableDef()) + " (" +
                getColumnName(fKDefinition.getRemote().getColumnName()) + ") " +
                getFKConstraint(fKDefinition.getConstraint());
    }

    protected abstract String getDataType(ColumnDefinition columnDefinition, int maxLength);
}
