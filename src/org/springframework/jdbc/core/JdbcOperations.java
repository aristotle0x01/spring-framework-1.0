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

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JDBC operations.
 * Implemented by JdbcTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Alternatively, the standard JDBC infrastructure can be mocked.
 * However, mocking this interface constitutes significantly less work.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: JdbcOperations.java,v 1.8 2004/03/23 22:48:38 jhoeller Exp $
 * @see JdbcTemplate
 */
public interface JdbcOperations {

	//-------------------------------------------------------------------------
	// Methods dealing with static SQL (java.sql.Statement)
	//-------------------------------------------------------------------------

	/**
	 * Execute the action specified by the given action object within a JDBC
	 * Statement. Allows for returning a result object, i.e. a domain object
	 * or a collection of domain objects.
	 * @param action callback object that specifies the action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException if there is any problem
	 */
	Object execute(final StatementCallback action) throws DataAccessException;

	/**
	 * Issue a single SQL execute, typically a DDL statement.
	 * @param sql static SQL to execute
	 * @throws DataAccessException if there is any problem
	 */
	void execute(final String sql) throws DataAccessException;

	/**
	 * Execute a query given static SQL, reading the ResultSet with a
	 * ResultSetExtractor.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with a null PreparedStatementSetter as a parameter.
	 * @param sql SQL query to execute
	 * @param rse object that will extract all rows of results
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #query(String, PreparedStatementSetter, ResultSetExtractor)
	 */
	Object query(final String sql, final ResultSetExtractor rse) throws DataAccessException;

	/**
	 * Execute a query given static SQL, reading the ResultSet on a per-row
	 * basis with a RowCallbackHandler (potentially implementing the ResultReader
	 * sub-interface that provides a result List).
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with null as PreparedStatementSetter argument.
	 * @param sql SQL query to execute
	 * @param rch object that will extract results
	 * @return the result List in case of a ResultReader, or null else
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #query(String, PreparedStatementSetter, RowCallbackHandler)
	 */
	List query(String sql, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * Execute a query for a result list, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForList
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The results will be mapped to an ArrayList (one entry for each row) of
	 * HashMaps (one entry for each column using the column name as the key).
	 * @param sql SQL query to execute
	 * @return an ArrayList that contains a HashMap per row
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForList(String, Object[])
	 */
	List queryForList(String sql) throws DataAccessException;

	/**
	 * Execute a query for a result object, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForObject
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL query to execute
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type, or null in case of SQL NULL
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForObject(String, Object[], Class)
	 */
	Object queryForObject(String sql, Class requiredType) throws DataAccessException;

	/**
	 * Execute a query that results in a long value, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForLong
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in a long value.
	 * @param sql SQL query to execute
	 * @return the long value, or 0 in case of SQL NULL
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForLong(String, Object[])
	 */
	long queryForLong(String sql) throws DataAccessException;

	/**
	 * Execute a query that results in an int value, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForInt
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in an int value.
	 * @param sql SQL query to execute
	 * @return the int value, or 0 in case of SQL NULL
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForInt(String, Object[])
	 */
	int queryForInt(String sql) throws DataAccessException;

	/**
	 * Issue a single SQL update.
	 * @param sql static SQL to execute
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem.
	 */
	int update(final String sql) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Methods dealing with prepared statements
	//-------------------------------------------------------------------------

	/**
	 * Execute the action specified by the given action object within a JDBC
	 * PreparedStatement. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * @param psc object that can create a PreparedStatement given a Connection
	 * @param action callback object that specifies the action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException if there is any problem
	 */
	Object execute(PreparedStatementCreator psc, PreparedStatementCallback action);

	/**
	 * Execute the action specified by the given action object within a JDBC
	 * PreparedStatement. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * @param sql SQL to execute
	 * @param action callback object that specifies the action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException if there is any problem
	 */
	Object execute(final String sql, PreparedStatementCallback action);

	/**
	 * Query using a prepared statement.
	 * @param psc object that can create a PreparedStatement given a Connection
	 * @param rse object that will extract results
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem
	 */
	Object query(PreparedStatementCreator psc, ResultSetExtractor rse);

	/**
	 * Query using a prepared statement, reading the ResultSet with a
	 * ResultSetExtractor.
	 * @param sql SQL to execute
	 * @param pss object that knows how to set values on the prepared statement.
	 * If this is null, the SQL will be assumed to contain no bind parameters.
	 * Even if there are no bind parameters, this object may be used to
	 * set fetch size and other performance options.
	 * @param rse object that will extract results.
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem
	 */
	Object query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor rse);

	/**
	 * Query using a prepared statement, reading the ResultSet on a per-row
	 * basis with a RowCallbackHandler (potentially implementing the ResultReader
	 * sub-interface that provides a result List).
	 * @param psc object that can create a PreparedStatement given a Connection
	 * @param rch object that will extract results,
	 * one row at a time
	 * @return the result List in case of a ResultReader, or null else
	 * @throws DataAccessException if there is any problem
	 */
	List query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException;
	
	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * PreparedStatementSetter implementation that knows how to bind values
	 * to the query, reading the ResultSet on a per-row basis with a
	 * RowCallbackHandler (potentially implementing the ResultReader
	 * sub-interface that provides a result List).
	 * @param sql SQL to execute
	 * @param pss object that knows how to set values on the prepared statement.
	 * If this is null, the SQL will be assumed to contain no bind parameters.
	 * Even if there are no bind parameters, this object may be used to
	 * set fetch size and other performance options.
	 * @param rch object that will extract results
	 * @return the result List in case of a ResultReader, or null else
	 * @throws DataAccessException if the query fails
	 */
	List query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch)
	    throws DataAccessException;
	
	/**
	 * Query given SQL to create a prepared statement from SQL and a list of
	 * arguments to bind to the query, reading the ResultSet on a per-row basis
	 * with a RowCallbackHandler (potentially implementing the ResultReader
	 * sub-interface that provides a result List).
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * @param argTypes SQL types of the arguments (constants from java.sql.Types)
	 * @param rch object that will extract results
	 * @return the result List in case of a ResultReader, or null else
	 * @throws DataAccessException if the query fails
	 * @see java.sql.Types
	 */
	List query(String sql, final Object[] args, final int[] argTypes, RowCallbackHandler rch)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list of
	 * arguments to bind to the query, reading the ResultSet on a per-row basis
	 * with a RowCallbackHandler (potentially implementing the ResultReader
	 * sub-interface that provides a result List).
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @param rch object that will extract results
	 * @return the result List in case of a ResultReader, or null else
	 * @throws DataAccessException if the query fails
	 */
	List query(String sql, final Object[] args, RowCallbackHandler rch)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The results will be mapped to an ArrayList (one entry for each row) of
	 * HashMaps (one entry for each column using the column name as the key).
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return an ArrayList that contains a HashMap per row
	 * @throws DataAccessException if the query fails
	 * @see #queryForList(String)
	 */
	List queryForList(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result object.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type, or null in case of SQL NULL
	 * @throws DataAccessException if the query fails
	 * @see #queryForObject(String, Class)
	 */
	Object queryForObject(String sql, final Object[] args, Class requiredType)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in a long value.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in a long value.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the long value, or 0 in case of SQL NULL
	 * @throws DataAccessException if the query fails
	 * @see #queryForLong(String)
	 */
	long queryForLong(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in an int value.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in an int value.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the int value, or 0 in case of SQL NULL
	 * @throws DataAccessException if the query fails
	 * @see #queryForInt(String)
	 */
	int queryForInt(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Issue an update using a PreparedStatementCreator to provide SQL and any
	 * required parameters.
	 * @param psc object that provides SQL and any necessary parameters
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(PreparedStatementCreator psc) throws DataAccessException;

	/**
	 * Issue an update using a PreparedStatementSetter to set bind parameters,
	 * with given SQL. Simpler than using a PreparedStatementCreator as this
	 * method will create the PreparedStatement: The PreparedStatementSetter
	 * just needs to set parameters.
	 * @param sql SQL, containing bind parameters
	 * @param pss helper that sets bind parameters. If this is null
	 * we run an update with static SQL.
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(final String sql, final PreparedStatementSetter pss) throws DataAccessException;
	
	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param args arguments to bind to the query
	 * @param argTypes SQL types of the arguments (constants from java.sql.Types)
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, final Object[] args, final int[] argTypes) throws DataAccessException;

	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Issue multiple updates on a single PreparedStatement, using JDBC 2.0
	 * batch updates and a BatchPreparedStatementSetter to set values.
	 * <p>Will fall back to separate updates on a single PreparedStatement
	 * if the JDBC driver does not support batch updates.
	 * @param sql defining PreparedStatement that will be reused.
	 * All statements in the batch will use the same SQL.
	 * @param pss object to set parameters on the PreparedStatement
	 * created by this method
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Methods dealing with callable statements
	//-------------------------------------------------------------------------

	/**
	 * Execute the action specified by the given action object within a JDBC
	 * CallableStatement. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * @param csc object that can create a CallableStatement given a Connection
	 * @param action callback object that specifies the action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException if there is any problem
	 */
	Object execute(CallableStatementCreator csc, CallableStatementCallback action);

	/**
	 * Execute the action specified by the given action object within a JDBC
	 * CallableStatement. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * @param callString the SQL call string to execute
	 * @param action callback object that specifies the action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException if there is any problem
	 */
	Object execute(final String callString, CallableStatementCallback action);

	/**
	 * Execute a SQL call using a CallableStatementCreator to provide SQL and any
	 * required parameters.
	 * @param csc object that provides SQL and any necessary parameters
	 * @return Map of extracted out parameters
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	Map call(CallableStatementCreator csc, List declaredParameters) throws DataAccessException;
	
}
