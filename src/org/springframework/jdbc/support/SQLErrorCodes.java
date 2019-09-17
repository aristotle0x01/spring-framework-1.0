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

package org.springframework.jdbc.support;

/**
 * JavaBean for holding JDBC Error Codes for a particular database.
 * Normally loaded through a BeanFactory
 * implementation. Used by the SQLErrorCodeSQLExceptionTranslator.
 * @author Thomas Risberg
 * @version $Id: SQLErrorCodes.java,v 1.4 2004/03/18 02:46:15 trisberg Exp $
 */
public class SQLErrorCodes {

	private String databaseProductName = null;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];
	
	private String[] dataRetrievalFailureCodes = new String[0];
	
	private String[] optimisticLockingFailureCodes = new String[0];
	
	private String[] dataAccessResourceFailureCodes = new String[0];

	/**
	 * Sets the badSqlGrammarCodes.
	 */
	public void setBadSqlGrammarCodes(String[] badSqlGrammarCodes) {
		this.badSqlGrammarCodes = badSqlGrammarCodes;
	}

	/**
	 * Returns the badSqlGrammarCodes.
	 */
	public String[] getBadSqlGrammarCodes() {
		return badSqlGrammarCodes;
	}

	/**
	 * Sets the dataIntegrityViolationCodes.
	 */
	public void setDataIntegrityViolationCodes(String[] dataIntegrityViolationCodes) {
		this.dataIntegrityViolationCodes = dataIntegrityViolationCodes;
	}

	/**
	 * Returns the dataIntegrityViolationCodes.
	 */
	public String[] getDataIntegrityViolationCodes() {
		return dataIntegrityViolationCodes;
	}

	/**
	 * @return Returns the dataRetrievalFailureCodes.
	 */
	public String[] getDataRetrievalFailureCodes() {
		return dataRetrievalFailureCodes;
	}
	/**
	 * @param dataRetrievalFailureCodes The dataRetrievalFailureCodes to set.
	 */
	public void setDataRetrievalFailureCodes(String[] dataRetrievalFailureCodes) {
		this.dataRetrievalFailureCodes = dataRetrievalFailureCodes;
	}

	/**
	 * @return Returns the dataAccessResourceFailureCodes.
	 */
	public String[] getDataAccessResourceFailureCodes() {
		return dataAccessResourceFailureCodes;
	}

	/**
	 * @param dataAccessResourceFailureCodes The dataAccessResourceFailureCodes to set.
	 */
	public void setDataAccessResourceFailureCodes(
			String[] dataAccessResourceFailureCodes) {
		this.dataAccessResourceFailureCodes = dataAccessResourceFailureCodes;
	}

	/**
	 * @return Returns the optimisticLockingFailureCodes.
	 */
	public String[] getOptimisticLockingFailureCodes() {
		return optimisticLockingFailureCodes;
	}
	
	/**
	 * @param optimisticLockingFailureCodes The optimisticLockingFailureCodes to set.
	 */
	public void setOptimisticLockingFailureCodes(
			String[] optimisticLockingFailureCodes) {
		this.optimisticLockingFailureCodes = optimisticLockingFailureCodes;
	}

	/**
	 * @return Returns the databaseProductName.
	 */
	public String getDatabaseProductName() {
		return databaseProductName;
	}

	/**
	 * @param databaseProductName The databaseProductName to set.
	 */
	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

}
