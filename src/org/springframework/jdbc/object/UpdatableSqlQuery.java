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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ResultReader;

/**
 * Reusable RDBMS query in which concrete subclasses must implement
 * the abstract updateRow(ResultSet, int, context) method to update each 
 * row of the JDBC ResultSet and optionally map contents into an object.
 *
 * <p>Subclasses can be constructed providing SQL, parameter types
 * and a DataSource. SQL will often vary between subclasses.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class UpdatableSqlQuery extends SqlQuery {

	/**
	 * Constructor to allow use as a JavaBean
	 */
	public UpdatableSqlQuery() {
		setUpdatableResults(true);
	}

	/**
	 * Convenient constructor with DataSource and SQL string.
	 * @param ds DataSource to use to get connections
	 * @param sql SQL to run
	 */
	public UpdatableSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
		setUpdatableResults(true);
	}

	/**
	 * Implementation of protected abstract method. This invokes the subclass's
	 * implementation of the updateRow() method.
	 */
	protected ResultReader newResultReader(int rowsExpected, Object[] parameters, Map context) {
		return new ResultReaderImpl(rowsExpected, context);
	}

	/**
	 * Subclasses must implement this method to update each row of the 
	 * ResultSet and optionally create object of the result type.
	 * @param rs ResultSet we're working through
	 * @param rowNum row number (from 0) we're up to
	 * @param context passed to the execute() method.
	 * It can be null if no contextual information is need.  If you
	 * need to pass in data for each row, you can pass in a HashMap with 
	 * the primary key of the row being the key for the HashMap.  That way
	 * it is easy to locate the updates for each row 
	 * @return an object of the result type
	 * @throws SQLException if there's an error updateing data.
	 * Subclasses can simply not catch SQLExceptions, relying on the
	 * framework to clean up.
	 */
	protected abstract Object updateRow(ResultSet rs, int rowNum, Map context) throws SQLException;


	/**
	 * Implementation of ResultReader that calls the enclosing
	 * class's updateRow() method for each row.
	 */
	protected class ResultReaderImpl implements ResultReader {

		/** List to save results in */
		private List results;

		private Map context;

		private int rowNum = 0;

		/**
		 * Use an array results. More efficient if we know how many results to expect.
		 */
		public ResultReaderImpl(int rowsExpected, Map context) {
			// use the more efficient collection if we know how many rows to expect
			this.results = (rowsExpected > 0) ? (List) new ArrayList(rowsExpected) : (List) new LinkedList();
			this.context = context;
		}

		public void processRow(ResultSet rs) throws SQLException {
			this.results.add(updateRow(rs, this.rowNum++, this.context));
			rs.updateRow();
		}

		public List getResults() {
			return this.results;
		}
	}

}
