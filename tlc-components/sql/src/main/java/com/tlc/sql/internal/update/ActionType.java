package com.tlc.sql.internal.update;

public final class ActionType
{
    public enum PreActionType
    {
        CREATE_TABLE,
        CREATE_COLUMN_NULLABLE,
        INCREASE_COLUMN_LENGTH,
        INCREASE_DECIMAL_COLUMN_WEIGHT,
        CHANGE_COLUMN_DATA_TYPE,
        UPDATE_COLUMN_NULLABLE,
        DROP_UNIQUE_KEY,
        CREATE_INDEX,
        UPDATE_INDEX,
        DELETE_INDEX
    }

    public enum PostActionType
    {
        UPDATE_COLUMN_NOT_NULLABLE,
        DELETE_COLUMN,
        CREATE_UNIQUE_KEY,
        UPDATE_UNIQUE_KEY,
        CREATE_FOREIGN_KEY,
        DROP_FOREIGN_KEY,
        UPDATE_FOREIGN_KEY,
        DROP_TABLE
    }
}



