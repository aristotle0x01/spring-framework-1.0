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

package org.springframework.jdbc.object;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;

/**
 * RdbmsOperation subclass representing a SQL update.
 * Like a query, an update object is reusable. Like all RdbmsOperation
 * objects, an update can have parameters and is defined in SQL.
 *
 * <p>This class provides a number of update() methods analogous to the
 * execute() methods of query objects.
 *
 * <p>This class is concrete. Although it can be subclassed (for example
 * to add a custom update method) it can easily be parameterized by setting
 * SQL and declaring parameters.
 *
 * @author Rod Johnson
 * @author Isabelle Muszynski
 * @version $Id: SqlUpdate.java,v 1.5 2004/03/18 02:46:13 trisberg Exp $
 */
public class SqlUpdate extends SqlOperation {

	/**
	 * Maximum number of rows the update may affect.
	 * If more are affected, an exception will be thrown.
	 * Ignored if 0.
	 */
	private int maxRowsAffected;

	/**
	 * An exact number of rows that must be affected
	 */
	private int requiredRowsAffected;


	/**
	 * Constructor to allow use as a JavaBean. DataSource,
	 * SQL and any parameter declarations must be supplied before
	 * compilation and use.
	 */
	public SqlUpdate() {
	}

	/**
	 * Constructs an update object with a given DataSource and SQL
	 * to keep consistent with the
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL
	 */
	public SqlUpdate(DataSource ds, String sql) {
		this(ds, sql, null, Integer.MAX_VALUE);
	}

	/**
	 * Construct an update object with a given DataSource, SQL
	 * and anonymous parameters
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL
	 * @param types anonymous parameter declarations.
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types) {
		this(ds, sql, types, Integer.MAX_VALUE);
	}

	/**
	 * Construct an update object with a given DataSource, SQL,
	 * anonymous parameters and specifying the maximum number of rows that may
	 * be affected.
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL
	 * @param types anonymous parameter declarations.
	 * @param maxRowsAffected the maximum number of rows that may
	 * be affected by the update.
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types, int maxRowsAffected) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.maxRowsAffected = maxRowsAffected;
	}


	/**
	 * Set the maximum number of rows that may be affected
	 * by this update. The default value is 0, which does not
	 * limit the number of rows affected.
	 * @param max the maximum number of rows that can be affected
	 * by this update without this class's update() method considering
	 * it an error.
	 */
	public void setMaxRowsAffected(int max) {
		this.maxRowsAffected = max;
	}

	/**
	 * Set the <i>exact</i> number of rows that must be affected by this update.
	 * The default value is 0, which allows any number of rows to be affected.
	 * An alternative to setting the <i>maximum</i> number of rows that
	 * may be affected.
	 * @param rowsAffected the exact number of rows that must be
	 * affected by this update.
	 */
	public void setRequiredRowsAffected(int rowsAffected) {
		this.requiredRowsAffected = rowsAffected;
	}


	/**
	 * Generic method to execute the update given arguments.
	 * All other update() methods invoke this method.
	 * @param args array of object arguments
	 * @return the number of rows affected by the update
	 */
	public int update(Object[] args) throws InvalidDataAccessApiUsageException {
		validateParameters(args);

		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(args));
		logger.debug("Executing update statement: " + getSql());

		if (this.maxRowsAffected != 0 && rowsAffected > this.maxRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(getSql(), this.maxRowsAffected, rowsAffected);
		}
		if (this.requiredRowsAffected != 0 && rowsAffected != this.requiredRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(getSql(), this.requiredRowsAffected, rowsAffected);
		}

		logger.debug(rowsAffected + " rows affected by SQL update [" + getSql() + "]");
		return rowsAffected;
	}

	/**
	 * Convenience method to execute an update with no parameters.
	 */
	public int update() {
		return update((Object[]) null);
	}

	/**
	 * Convenient method to execute an update given one int arg.
	 */
	public int update(int p1) {
		return update(new Object[]{new Integer(p1)});
	}

	/**
	 * Convenient method to execute an update given two int args.
	 */
	public int update(int p1, int p2) {
		return update(new Object[]{new Integer(p1), new Integer(p2)});
	}

	/**
	 * Convenient method to execute an update given one String arg.
	 */
	public int update(String p) {
		return update(new Object[]{p});
	}

	/**
	 * Convenient method to execute an update given two String args.
	 */
	public int update(String p1, String p2) {
		return update(new Object[]{p1, p2});
	}

}
