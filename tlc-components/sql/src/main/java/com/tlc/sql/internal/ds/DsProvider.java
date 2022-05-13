package com.tlc.sql.internal.ds;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DsProvider
{
	void close();

	boolean connect();

	Connection getConnection() throws SQLException;
}
