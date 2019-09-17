/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.jdbc.support.nativejdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.enhydra.jdbc.core.CoreConnection;
import org.enhydra.jdbc.core.CorePreparedStatement;

/**
 * Implementation of the NativeJdbcExtractor interface for ObjectWeb's XAPool
 * connection pool. Returns underlying native Connections and native PreparedStatements
 * to application code instead of XAPool's wrapper implementations; unwraps the
 * Connection for native Statements and native CallableStatements.
 * The returned JDBC classes can then safely be cast, e.g. to OracleResultSet.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with a XAPool
 * DataSource: If a given object is not an XAPool wrapper, it will be returned as-is.
 *
 * @author Juergen Hoeller
 * @since 06.02.2004
 */
public class XAPoolNativeJdbcExtractor implements NativeJdbcExtractor {

	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return false;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	public Connection getNativeConnection(Connection con) throws SQLException {
		if (con instanceof CoreConnection) {
			return ((CoreConnection) con).con;
		}
		return con;
	}

	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return getNativeConnection(stmt.getConnection());
	}

	public Statement getNativeStatement(Statement stmt) throws SQLException {
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		if (ps instanceof CorePreparedStatement) {
			return ((CorePreparedStatement) ps).ps;
		}
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		return cs;
	}

	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
