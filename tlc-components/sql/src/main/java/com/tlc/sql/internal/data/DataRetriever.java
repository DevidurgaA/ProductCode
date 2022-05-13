package com.tlc.sql.internal.data;

import com.tlc.sql.api.meta.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DataRetriever
{
    Object getData(ResultSet resultSet, int position, DataType dataType) throws SQLException;
}
