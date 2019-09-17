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

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

/**
 * <b>This is the central class in the JDBC core package.</b>
 * It simplifies the use of JDBC and helps to avoid common errors.
 * It executes core JDBC workflow, leaving application code to provide SQL
 * and extract results. This class executes SQL queries or updates, initiating
 * iteration over ResultSets and catching JDBC exceptions and translating
 * them to the generic, more informative exception hierarchy defined in the
 * org.springframework.dao package.
 *
 * <p>Code using this class need only implement callback interfaces, giving
 * them a clearly defined contract. The PreparedStatementCreator callback
 * interface creates a prepared statement given a Connection provided by this
 * class, providing SQL and any necessary parameters. The RowCallbackHandler
 * interface extracts values from each row of a ResultSet.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a DataSource reference, or get prepared in an application context
 * and given to services as bean reference. Note: The DataSource should
 * always be configured as a bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>The motivation and design of this class is discussed
 * in detail in
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>Because this class is parameterizable by the callback interfaces and
 * the SQLExceptionTranslator interface, it isn't necessary to subclass it.
 * All SQL issued by this class is logged.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Yann Caroff
 * @author Thomas Risberg
 * @author Isabelle Muszynski
 * @version $Id: JdbcTemplate.java,v 1.37 2004/03/23 22:48:38 jhoeller Exp $
 * @since May 3, 2001
 * @see org.springframework.dao
 * @see org.springframework.jdbc.datasource
 * @see org.springframework.jdbc.object
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Custom NativeJdbcExtractor */
	private NativeJdbcExtractor nativeJdbcExtractor;

	/** If this variable is false, we will throw exceptions on SQL warnings */
	private boolean ignoreWarnings = true;


	/**
	 * Construct a new JdbcTemplate for bean usage.
	 * Note: The DataSource has to be set before using the instance.
	 * This constructor can be used to prepare a JdbcTemplate via a BeanFactory,
	 * typically setting the DataSource via setDataSource.
	 * @see #setDataSource
	 */
	public JdbcTemplate() {
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * Note: This will trigger eager initialization of the exception translator.
	 * @param dataSource JDBC DataSource to obtain connections from
	 */
	public JdbcTemplate(DataSource dataSource) {
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	/**
	 * Set a NativeJdbcExtractor to extract native JDBC objects from wrapped handles.
	 * Useful if native Statement and/or ResultSet handles are expected for casting
	 * to database-specific implementation classes, but a connection pool that wraps
	 * JDBC objects is used (note: <i>any</i> pool will return wrapped Connections).
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		this.nativeJdbcExtractor = extractor;
	}

	/**
	 * Return the current NativeJdbcExtractor implementation.
	 */
	public NativeJdbcExtractor getNativeJdbcExtractor() {
		return this.nativeJdbcExtractor;
	}

	/**
	 * Set whether or not we want to ignore SQLWarnings.
	 * Default is true.
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	/**
	 * Return whether or not we ignore SQLWarnings.
	 * Default is true.
	 */
	public boolean getIgnoreWarnings() {
		return ignoreWarnings;
	}


	//-------------------------------------------------------------------------
	// Methods dealing with static SQL (java.sql.Statement)
	//-------------------------------------------------------------------------

	public Object execute(final StatementCallback action) {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
					this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativeStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			stmt = conToUse.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			Statement stmtToUse = stmt;
			if (this.nativeJdbcExtractor != null) {
				stmtToUse = this.nativeJdbcExtractor.getNativeStatement(stmt);
			}
			Object result = action.doInStatement(stmtToUse);
			SQLWarning warning = stmt.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
			return result;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("executing StatementCallback", null, ex);
		}
		finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	public void execute(final String sql) throws DataAccessException {
		execute(new StatementCallback() {
			public Object doInStatement(Statement stmt) throws SQLException {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing SQL [" + sql + "]");
				}
				stmt.execute(sql);
				return null;
			}
		});
	}

	public Object query(final String sql, final ResultSetExtractor rse) throws DataAccessException {
		if (sql == null) {
			throw new InvalidDataAccessApiUsageException("SQL may not be null");
		}
		if (sql.indexOf("?") != -1) {
			throw new InvalidDataAccessApiUsageException(
					"Cannot execute [" + sql + "] as a static query: it contains bind variables");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Executing static SQL query [" + sql + "] using a java.sql.Statement");
		}
		return execute(new StatementCallback() {
			public Object doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					ResultSet rsToUse = rs;
					if (nativeJdbcExtractor != null) {
						rsToUse = nativeJdbcExtractor.getNativeResultSet(rs);
					}
					return rse.extractData(rsToUse);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
		});
	}

	public List query(String sql, RowCallbackHandler rch) throws DataAccessException {
		return (List) query(sql, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public List queryForList(String sql) throws DataAccessException {
		return (List) query(sql, new ListResultSetExtractor());
	}

	public Object queryForObject(String sql, Class requiredType) throws DataAccessException {
		return query(sql, new ObjectResultSetExtractor(requiredType));
	}

	public long queryForLong(String sql) throws DataAccessException {
		Number number = (Number) queryForObject(sql, Number.class);
		return (number != null ? number.longValue() : 0);
	}

	public int queryForInt(String sql) throws DataAccessException {
		Number number = (Number) queryForObject(sql, Number.class);
		return (number != null ? number.intValue() : 0);
	}

	public int update(final String sql) throws DataAccessException {
		Integer result = (Integer) execute(new StatementCallback() {
			public Object doInStatement(Statement stmt) throws SQLException {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing SQL update [" + sql + "]");
				}
				int rows = stmt.executeUpdate(sql);
				if (logger.isDebugEnabled()) {
					logger.debug("SQL update affected " + rows + " rows");
				}
				return new Integer(rows);
			}
		});
		return result.intValue();
	}


	//-------------------------------------------------------------------------
	// Methods dealing with prepared statements
	//-------------------------------------------------------------------------

	public Object execute(PreparedStatementCreator psc, PreparedStatementCallback action) {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		PreparedStatement ps = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
					this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativePreparedStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			ps = psc.createPreparedStatement(conToUse);
			DataSourceUtils.applyTransactionTimeout(ps, getDataSource());
			PreparedStatement psToUse = ps;
			if (this.nativeJdbcExtractor != null) {
				psToUse = this.nativeJdbcExtractor.getNativePreparedStatement(ps);
			}
			Object result = action.doInPreparedStatement(psToUse);
			SQLWarning warning = ps.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
			return result;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("executing PreparedStatementCallback [" + psc + "]",
																							 getSql(psc), ex);
		}
		finally {
			JdbcUtils.closeStatement(ps);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	public Object execute(final String sql, PreparedStatementCallback action) {
		return execute(new SimplePreparedStatementCreator(sql), action);
	}

	/**
	 * Query using a prepared statement, allowing for a PreparedStatementCreator
	 * and a PreparedStatementSetter. Most other query methods use this method,
	 * but application code will always work with either a creator or a setter.
	 * @param psc Callback handler that can create a PreparedStatement given a
	 * Connection
	 * @param pss object that knows how to set values on the prepared statement.
	 * If this is null, the SQL will be assumed to contain no bind parameters.
	 * @param rse object that will extract results.
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem
	 */
	protected Object query(PreparedStatementCreator psc, final PreparedStatementSetter pss,
												 final ResultSetExtractor rse) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL query using PreparedStatementCreator [" + psc + "]");
		}
		return execute(psc, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				if (pss != null) {
					pss.setValues(ps);
				}
				ResultSet rs = null;
				try {
					rs = ps.executeQuery();
					ResultSet rsToUse = rs;
					if (nativeJdbcExtractor != null) {
						rsToUse = nativeJdbcExtractor.getNativeResultSet(rs);
					}
					return rse.extractData(rsToUse);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
		});
	}

	public Object query(PreparedStatementCreator psc, ResultSetExtractor rse) {
		return query(psc, null, rse);
	}

	public Object query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor rse)
			throws DataAccessException {
		if (sql == null) {
			throw new InvalidDataAccessApiUsageException("SQL may not be null");
		}
		return query(new SimplePreparedStatementCreator(sql), pss, rse);
	}

	public List query(PreparedStatementCreator psc, RowCallbackHandler rch)
			throws DataAccessException {
		return (List) query(psc, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public List query(String sql, final PreparedStatementSetter pss, final RowCallbackHandler rch)
			throws DataAccessException {
		return (List) query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public List query(String sql, final Object[] args, final int[] argTypes, RowCallbackHandler rch)
			throws DataAccessException {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
				(args != null && args.length != argTypes.length)) {
			throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
		}
		return query(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i], argTypes[i]);
					}
				}
			}
		}, rch);
	}

	public List query(String sql, final Object[] args, RowCallbackHandler rch)
			throws DataAccessException {
		return query(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i]);
					}
				}
			}
		}, rch);
	}

	public List queryForList(String sql, final Object[] args) throws DataAccessException {
		return (List) query(sql,
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								ps.setObject(i + 1, args[i]);
							}
						}
					}
				},
				new ListResultSetExtractor());
	}

	public Object queryForObject(String sql, final Object[] args, Class requiredType)
			throws DataAccessException {
		return query(sql,
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								ps.setObject(i + 1, args[i]);
							}
						}
					}
				},
				new ObjectResultSetExtractor(requiredType));
	}

	public long queryForLong(String sql, final Object[] args) throws DataAccessException {
		Number number = (Number) queryForObject(sql, args, Number.class);
		return (number != null ? number.longValue() : 0);
	}

	public int queryForInt(String sql, final Object[] args) throws DataAccessException {
		Number number = (Number) queryForObject(sql, args, Number.class);
		return (number != null ? number.intValue() : 0);
	}

	protected int update(PreparedStatementCreator psc, final PreparedStatementSetter pss) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL update using PreparedStatementCreator [" + psc + "]");
		}
		Integer result = (Integer) execute(psc, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				if (pss != null) {
					pss.setValues(ps);
				}
				int rows = ps.executeUpdate();
				if (logger.isDebugEnabled()) {
					logger.debug("SQL update affected " + rows + " rows");
				}
				return new Integer(rows);
			}
		});
		return result.intValue();
	}

	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return update(psc, null);
	}

	public int update(String sql, final PreparedStatementSetter pss) throws DataAccessException {
		return update(new SimplePreparedStatementCreator(sql), pss);
	}

	public int update(String sql, final Object[] args, final int[] argTypes) throws DataAccessException {
		return update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i], argTypes[i]);
					}
				}
			}
		});
	}

	public int update(String sql, final Object[] args) throws DataAccessException {
		return update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i]);
					}
				}
			}
		});
	}

	public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
		return (int[]) execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				int batchSize = pss.getBatchSize();
				if (ps.getConnection().getMetaData().supportsBatchUpdates()) {
					for (int i = 0; i < batchSize; i++) {
						pss.setValues(ps, i);
						ps.addBatch();
					}
					return ps.executeBatch();
				}
				else {
					int[] rowsAffected = new int[batchSize];
					for (int i = 0; i < batchSize; i++) {
						pss.setValues(ps, i);
						rowsAffected[i] = ps.executeUpdate();
					}
					return rowsAffected;
				}
			}
		});
	}


	//-------------------------------------------------------------------------
	// Methods dealing with callable statements
	//-------------------------------------------------------------------------

	public Object execute(CallableStatementCreator csc, CallableStatementCallback action) {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		CallableStatement cs = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
					this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativeCallableStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			cs = csc.createCallableStatement(conToUse);
			DataSourceUtils.applyTransactionTimeout(cs, getDataSource());
			CallableStatement csToUse = cs;
			if (nativeJdbcExtractor != null) {
				csToUse = nativeJdbcExtractor.getNativeCallableStatement(cs);
			}
			Object result = action.doInCallableStatement(csToUse);
			SQLWarning warning = cs.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
			return result;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("executing CallableStatementCallback [" + csc + "]",
																							 getSql(csc), ex);
		}
		finally {
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	public Object execute(final String callString, CallableStatementCallback action) {
		return execute(new SimpleCallableStatementCreator(callString), action);
	}

	public Map call(CallableStatementCreator csc, final List declaredParameters) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing call using CallableStatementCreator [" + csc + "]");
		}
		return (Map) execute(csc, new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs) throws SQLException {
				boolean retVal = cs.execute();
				if (logger.isDebugEnabled()) {
					logger.debug("CallableStatement.execute returned [" + retVal + "]");
				}
				Map retMap = new HashMap();
				if (retVal) {
					retMap.putAll(extractReturnedResultSets(cs, declaredParameters));
				}
				retMap.putAll(extractOutputParameters(cs, declaredParameters));
				return retMap;
			}
		});
	}

	/**
	 * Extract output parameters from the completed stored procedure.
	 * @param cs JDBC wrapper for the stored procedure
	 * @param parameters parameter list for the stored procedure
	 * @return parameters to the stored procedure
	 */
	protected Map extractOutputParameters(CallableStatement cs, List parameters) throws SQLException {
		Map outParams = new HashMap();
		int sqlColIndex = 1;
		for (int i = 0; i < parameters.size(); i++) {
			SqlParameter param = (SqlParameter) parameters.get(i);
			if (param instanceof SqlOutParameter) {
				SqlOutParameter outParam = (SqlOutParameter) param;
				Object out = cs.getObject(sqlColIndex);
				if (out instanceof ResultSet) {
					ResultSet outRs = (ResultSet) out;
					ResultSet rsToUse = outRs;
					if (this.nativeJdbcExtractor != null) {
						rsToUse = this.nativeJdbcExtractor.getNativeResultSet(rsToUse);
					}
					// we can't pass back a ResultSet since the connection will be closed - we must process it
					try {
						if (outParam.isResultSetSupported()) {
							RowCallbackHandler rch = null;
							if (outParam.isRowMapperSupported()) {
								rch = outParam.newResultReader();
							}
							else {
								rch = outParam.getRowCallbackHandler();
							}
							(new RowCallbackHandlerResultSetExtractor(rch)).extractData(rsToUse);
							logger.debug("ResultSet returned from stored procedure was processed");
							if (outParam.isRowMapperSupported()) {
								outParams.put(param.getName(), ((ResultReader) rch).getResults());
							}
							else {
								outParams.put(param.getName(), "ResultSet processed.");
							}
						}
						else {
							logger.warn("ResultSet returned from stored procedure but a corresponding SqlOutParameter with a RowCallbackHandler was not declared");
							outParams.put(param.getName(), "ResultSet was returned but not processed.");
						}
					}
					finally {
						JdbcUtils.closeResultSet(outRs);
					}
				}
				else {
					outParams.put(param.getName(), out);
				}
			}
			if (!(param instanceof SqlReturnResultSet)) {
				sqlColIndex++;
			}
		}
		return outParams;
	}

	/**
	 * Extract returned resultsets from the completed stored procedure.
	 * @param cs JDBC wrapper for the stored procedure
	 * @param parameters Parameter list for the stored procedure
	 */
	protected Map extractReturnedResultSets(CallableStatement cs, List parameters) throws SQLException {
		Map returnedResults = new HashMap();
		int rsIndex = 0;
		do {
			SqlParameter param = null;
			if (parameters != null && parameters.size() > rsIndex) {
				param = (SqlParameter) parameters.get(rsIndex);
			}
			if (param instanceof SqlReturnResultSet) {
				SqlReturnResultSet rsParam = (SqlReturnResultSet) param;
				ResultSet rs = cs.getResultSet();
				try {
					ResultSet rsToUse = rs;
					if (this.nativeJdbcExtractor != null) {
						rsToUse = this.nativeJdbcExtractor.getNativeResultSet(rs);
					}
					RowCallbackHandler rch = null;
					if (rsParam.isRowMapperSupported()) {
						rch = rsParam.newResultReader();
					}
					else {
						rch = rsParam.getRowCallbackHandler();
					}
					(new RowCallbackHandlerResultSetExtractor(rch)).extractData(rsToUse);
					if (rsParam.isRowMapperSupported()) {
						returnedResults.put(param.getName(), ((ResultReader) rch).getResults());
					}
					else {
						returnedResults.put(param.getName(), "ResultSet returned from stored procedure was processed");
					}
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
			else {
				logger.warn("ResultSet returned from stored procedure but a corresponding SqlReturnResultSet parameter was not declared");
			}
			rsIndex++;
		}
		while (cs.getMoreResults());
		return returnedResults;
	}


	/**
	 * Throw an SQLWarningException if we're not ignoring warnings.
	 * @param warning warning from current statement. May be null,
	 * in which case this method does nothing.
	 */
	protected void throwExceptionOnWarningIfNotIgnoringWarnings(SQLWarning warning) throws SQLWarningException {
		if (warning != null) {
			if (this.ignoreWarnings) {
				logger.warn("SQLWarning ignored: " + warning);
			}
			else {
				throw new SQLWarningException("Warning not ignored", warning);
			}
		}
	}

	/**
	 * Determine SQL from potential provider object.
	 * @param sqlProvider object that's potentially a SqlProvider
	 * @return the SQL string, or null
	 * @see SqlProvider
	 */
	protected String getSql(Object sqlProvider) {
		if (sqlProvider instanceof SqlProvider) {
			return ((SqlProvider) sqlProvider).getSql();
		}
		else {
			return null;
		}
	}


	/**
	 * Simple adapter for PreparedStatementCreator, allowing to use a plain SQL statement.
	 */
	protected static final class SimplePreparedStatementCreator
			implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public SimplePreparedStatementCreator(String sql) {
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql);
		}

		public String getSql() {
			return sql;
		}
	}


	/**
	 * Simple adapter for CallableStatementCreator, allowing to use a plain SQL statement.
	 */
	protected static final class SimpleCallableStatementCreator
			implements CallableStatementCreator, SqlProvider {

		private final String callString;

		public SimpleCallableStatementCreator(String callString) {
			this.callString = callString;
		}

		public CallableStatement createCallableStatement(Connection con) throws SQLException {
			return con.prepareCall(this.callString);
		}

		public String getSql() {
			return callString;
		}

	}


	/**
	 * Adapter to enable use of a RowCallbackHandler inside a ResultSetExtractor.
	 * <p>Uses a regular ResultSet, so we have to be careful when using it:
	 * We don't use it for navigating since this could lead to unpredictable consequences.
	 */
	protected static final class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor {

		private RowCallbackHandler rch;

		public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
			this.rch = rch;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.rch.processRow(rs);
			}
			if (this.rch instanceof ResultReader) {
				return ((ResultReader) this.rch).getResults();
			}
			else {
				return null;
			}
		}
	}


	/**
	 * ResultSetExtractor implementation that returns an ArrayList of HashMaps.
	 */
	protected static final class ListResultSetExtractor implements ResultSetExtractor {

		public Object extractData(ResultSet rs) throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			List listOfRows = new ArrayList();
			while (rs.next()) {
				Map mapOfColValues = new HashMap(numberOfColumns);
				for (int i = 1; i <= numberOfColumns; i++) {
					mapOfColValues.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				listOfRows.add(mapOfColValues);
			}
			if (listOfRows.size() == 1 && numberOfColumns == 1) {
				return ((Map) listOfRows.get(0)).get(rsmd.getColumnName(1));
			}
			else {
				return listOfRows;
			}
		}
	}


	/**
	 * ResultSetExtractor implementation that returns single result object.
	 */
	protected static final class ObjectResultSetExtractor implements ResultSetExtractor {

		private final Class requiredType;

		public ObjectResultSetExtractor(Class requiredType) {
			this.requiredType = requiredType;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int nrOfColumns = rsmd.getColumnCount();
			if (nrOfColumns != 1) {
				throw new InvalidDataAccessApiUsageException("Expected single column, but received " + nrOfColumns + " columns");
			}
			if (!rs.next()) {
				throw new InvalidDataAccessApiUsageException("Expected single row, not empty ResultSet");
			}
			Object result = rs.getObject(1);
			if (rs.next()) {
				throw new InvalidDataAccessApiUsageException("Expected single row, not more than one");
			}
			if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
				throw new InvalidDataAccessApiUsageException("Result object (db-type=\"" + rsmd.getColumnTypeName(1) +
																										 "\" value=\"" + result + "\") is of type [" +
																										 rsmd.getColumnClassName(1) + "] and not of required type [" +
																										 this.requiredType.getName() + "]");
			}
			return result;
		}
	}

}
