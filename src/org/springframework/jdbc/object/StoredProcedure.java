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

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.jdbc.core.SqlParameter;

/**
 * Superclass for object abstractions of RDBMS stored procedures.
 * This class is abstract and its execute methods are protected, preventing use other than through
 * a subclass that offers tighter typing.
 *
 * <p>The inherited <code>sql</code> property is the name of the stored procedure in the RDBMS.
 * Note that JDBC 3.0 introduces named parameters, although the other features provided
 * by this class are still necessary in JDBC 3.0.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @version $Id: StoredProcedure.java,v 1.10 2004/03/18 02:46:13 trisberg Exp $
 */
public abstract class StoredProcedure extends SqlCall {

	/**
	 * Allow use as a bean.
	 */
	protected StoredProcedure() {
	}

	/**
	 * Create a new object wrapper for a stored procedure.
	 * @param ds DataSource to use throughout the lifetime
	 * of this object to obtain connections
	 * @param name name of the stored procedure in the database.
	 */
	protected StoredProcedure(DataSource ds, String name) {
		setDataSource(ds);
		setSql(name);
	}
	
	/**
	 * Create a new object wrapper for a stored procedure.
	 * @param jdbcTemplate JdbcTemplate which wraps DataSource
	 * @param name name of the stored procedure in the database.
	 */
	protected StoredProcedure(JdbcTemplate jdbcTemplate, String name) {
		setJdbcTemplate(jdbcTemplate);
		setSql(name);
	}

	/**
	 * Overridden method. Add a parameter.
	 * <b>NB: Calls to addParameter must be made in the same
	 * order as they appear in the database's stored procedure parameter
	 * list.</b> Names are purely used to help mapping
	 * @param p Parameter object (as defined in the Parameter
	 * inner class)
	 */
	public void declareParameter(SqlParameter p) throws InvalidDataAccessApiUsageException {
		if (p.getName() == null) {
			throw new InvalidDataAccessApiUsageException("Parameters to stored procedures must have names as well as types");
		}
		super.declareParameter(p);
	}

	/**
	 * Execute the stored procedure. Subclasses should define a strongly typed
	 * execute method (with a meaningful name) that invokes this method, populating
	 * the input map and extracting typed values from the output map. Subclass
	 * execute methods will often take domain objects as arguments and return values.
	 * Alternatively, they can return void.
	 * @param inParams map of input parameters, keyed by name as in parameter
	 * declarations. Output parameters need not (but can be) included in this map.
	 * It is legal for map entries to be null, and this will produce the correct
	 * behavior using a NULL argument to the stored procedure.
	 * @return map of output params, keyed by name as in parameter declarations.
	 * Output parameters will appear here, with their values after the
	 * stored procedure has been called.
	 */
	public Map execute(final Map inParams) throws InvalidDataAccessApiUsageException {
		validateParameters(inParams.values().toArray());
		logger.debug("Executing call: " + getCallString());
		Map retValues = getJdbcTemplate().call(newCallableStatementCreator(inParams), this.getDeclaredParameters());
		return retValues;
	}

	/**
	 * Execute the stored procedure. Subclasses should define a strongly typed
	 * execute method (with a meaningful name) that invokes this method, passing in
	 * a ParameterMapper that will populate the input map.  This allows mapping database 
	 * specific features since the ParameterMapper has access to the Connection object.
	 * The execute method is also responsible for extracting typed values from the output map. 
	 * Subclass execute methods will often take domain objects as arguments and return values.
	 * Alternatively, they can return void.
	 * @param inParamMapper map of input parameters, keyed by name as in parameter
	 * declarations. Output parameters need not (but can be) included in this map.
	 * It is legal for map entries to be null, and this will produce the correct
	 * behavior using a NULL argument to the stored procedure.
	 * @return map of output params, keyed by name as in parameter declarations.
	 * Output parameters will appear here, with their values after the
	 * stored procedure has been called.
	 */
	public Map execute(final ParameterMapper inParamMapper) throws InvalidDataAccessApiUsageException {
		logger.debug("Executing call using ParameterMapper: " + getCallString());
		Map retValues = getJdbcTemplate().call(newCallableStatementCreator(inParamMapper), this.getDeclaredParameters());
		return retValues;
	}

}
