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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Helper class that can efficiently create multiple CallableStatementCreator
 * objects with different parameters based on a SQL statement and a single
 * set of parameter declarations.
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public class CallableStatementCreatorFactory { 

	/** The SQL call string, which won't change when the parameters change. */
	private final String callString;

	/** List of SqlParameter objects. May not be null. */
	private List declaredParameters = new LinkedList();

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	/**
	 * Create a new factory. Will need to add parameters
	 * via the addParameter() method or have no parameters.
	 */
	public CallableStatementCreatorFactory(String callString) {
		this.callString = callString;
	}

	/**
	 * Create a new factory with sql and the given parameters.
	 * @param callString the SQL call string
	 * @param declaredParameters list of SqlParameter objects
	 */
	public CallableStatementCreatorFactory(String callString, List declaredParameters) {
		this.callString = callString;
		this.declaredParameters = declaredParameters;
	}

	/**
	 * Add a new declared parameter.
	 * Order of parameter addition is significant.
	 */
	public void addParameter(SqlParameter param) {
		this.declaredParameters.add(param);
	}
		
	/**
	 * Set whether to use prepared statements that return a
	 * specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Set whether to use prepared statements capable of returning
	 * updatable ResultSets.
	 */
	public void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Return a new CallableStatementCreator instance given this parameters.
	 * @param inParams List of parameters. May be null.
	 */
	public CallableStatementCreator newCallableStatementCreator(Map inParams) {
		return new CallableStatementCreatorImpl(inParams != null ? inParams : new HashMap());
	}

	/**
	 * Return a new CallableStatementCreator instance given this parameter mapper.
	 * @param inParamMapper ParameterMapper implementation that will return a Map of parameters. May not be null.
	 */
	public CallableStatementCreator newCallableStatementCreator(ParameterMapper inParamMapper) {
		return new CallableStatementCreatorImpl(inParamMapper);
	}


	/**
	 * CallableStatementCreator implementation returned by this class.
	 */
	private class CallableStatementCreatorImpl implements CallableStatementCreator, SqlProvider {

		private Map inParameters;

		private ParameterMapper inParameterMapper;
		
		/**
		 * @param inParams list of SqlParameter objects. May not be null
		 */
		private CallableStatementCreatorImpl(final Map inParams) {
			this.inParameters = inParams;
			this.inParameterMapper = null;
		}

		/**
		 * @param inParamMapper ParameterMapper implementation for mapping input parameters. May not be null
		 */
		private CallableStatementCreatorImpl(final ParameterMapper inParamMapper) {
			this.inParameters = null;
			this.inParameterMapper = inParamMapper;
		}

		public CallableStatement createCallableStatement(Connection con) throws SQLException {
			/* If we were given a ParameterMapper - we must let the mapper do its thing to create the Map */
			if (inParameterMapper != null) {
				inParameters = inParameterMapper.createMap(con);
			}
			else {
				if (inParameters == null) {
					throw new InvalidDataAccessApiUsageException("A ParameterMapper or a Map of parameters must be provided");
				}
			}

			CallableStatement cs = null;
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
				cs = con.prepareCall(callString);
			}
			else {
				cs = con.prepareCall(callString, resultSetType,
														 updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
			}

			int sqlColIndx = 1;
			for (int i = 0; i < declaredParameters.size(); i++) {
				SqlParameter p = (SqlParameter) CallableStatementCreatorFactory.this.declaredParameters.get(i);
				if (!inParameters.containsKey(p.getName()) && !(p instanceof SqlOutParameter) && !(p instanceof SqlReturnResultSet)) {
					throw new InvalidDataAccessApiUsageException("Required input parameter '" + p.getName() + "' is missing");
				}
				// The value may still be null
				Object in = inParameters.get(p.getName());
				if (!(p instanceof SqlOutParameter) && !(p instanceof SqlReturnResultSet)) {
					// Input parameters must be supplied
					if (in == null && p.getTypeName() != null) {
						cs.setNull(sqlColIndx, p.getSqlType(), p.getTypeName());
					}
					else
						if (in != null) {
							cs.setObject(sqlColIndx, in, p.getSqlType());
						}
						else {
							cs.setNull(sqlColIndx, p.getSqlType());
						}
				}
				else {
					// It's an output parameter. Skip SqlReturnResultSet parameters
					// It need not (but may be) supplied by the caller.
					if (p instanceof SqlOutParameter) {
						if (p.getTypeName() != null) {
							cs.registerOutParameter(sqlColIndx, p.getSqlType(), p.getTypeName());
						}
						else {
							cs.registerOutParameter(sqlColIndx, p.getSqlType());
						}
						if (in != null) {
							cs.setObject(sqlColIndx, in, p.getSqlType());
						}
					}
				}
				if (!(p instanceof SqlReturnResultSet)) {
					sqlColIndx++;
				}
			}

			return cs;
		}

		public String getSql() {
			return callString;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer("CallableStatementCreatorFactory.CallableStatementCreatorImpl: sql=[");
			buf.append(callString);
			buf.append("]: params=[");
			if (inParameters != null) {
				buf.append(inParameters.toString());
			}
			buf.append(']');
			return buf.toString();
		}
	}

}
